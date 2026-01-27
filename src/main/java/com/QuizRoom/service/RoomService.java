package com.QuizRoom.service;

import com.QuizRoom.dto.QuestionDTO;
import com.QuizRoom.dto.QuestionViewDTO;
import com.QuizRoom.dto.RoomStateResponse;
import com.QuizRoom.entity.QuizAttempt;
import com.QuizRoom.entity.User;
import com.QuizRoom.repository.QuizAttemptRepository;
import com.QuizRoom.repository.UserRepository;
import com.QuizRoom.websocket.SocketEvent;
import com.QuizRoom.websocket.SocketEventType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class RoomService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final QuizSocketService quizSocketService;


    public void endRoom(String roomCode, Long hostId) {
        System.out.println("🔥 SERVICE endRoom() called");
        System.out.println("roomCode=" + roomCode + ", hostId=" + hostId);
        validateHost(roomCode, hostId);
        System.out.println("✅ HOST VALIDATED");
        redisTemplate.opsForHash()
                .put(roomMeta(roomCode), "status", "ENDED");

        persistAttemptsFromRedis(roomCode);
        System.out.println("✅ ATTEMPTS PERSISTED");
        cleanupRoom(roomCode);
        System.out.println("🔥 CLEANUP FINISHED");
    }

    public String createRoom(Long hostId) {
        final int MAX_RETRIES = 5;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            String roomCode = generateRoomCode();
            String metaKey = roomMeta(roomCode);

            // Atomically claim this room code
            Boolean claimed = redisTemplate.opsForHash()
                    .putIfAbsent(metaKey, "status", "CREATING");

            if (Boolean.TRUE.equals(claimed)) {

                redisTemplate.opsForHash().put(metaKey, "hostId", hostId.toString());
                redisTemplate.opsForHash().put(metaKey, "status", "ACTIVE");
                redisTemplate.opsForHash().put(metaKey, "createdAt", Instant.now().toString());

                redisTemplate.opsForSet()
                        .add(roomUsers(roomCode), hostId.toString());

                return roomCode;
            }

            // Optional: log collision (good for observability)
            // log.debug("Room code collision: {}", roomCode);
        }

        throw new IllegalStateException(
                "Unable to create room after " + MAX_RETRIES + " attempts"
        );
    }


    public String generateRoomCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    public void joinRoom(String roomCode, Long userId) {
        String metaKey = roomMeta(roomCode);

        if (Boolean.FALSE.equals(redisTemplate.hasKey(metaKey))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid room code");
        }

        String status = (String) redisTemplate.opsForHash().get(metaKey, "status");
        if (!"ACTIVE".equals(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room not active");
        }

        Long added = redisTemplate.opsForSet().add(roomUsers(roomCode), userId.toString());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (added == 1) {
            // 4️⃣ 🔥 BROADCAST PLAYER_JOINED EVENT (PLACE IT HERE)
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", user.getId());
            payload.put("name", user.getDisplayName());
            payload.put("score", 0);

            quizSocketService.broadcastPlayerJoined(roomCode, payload);

        }
    }

    public void publishQuestion(String roomCode, Long hostId, QuestionDTO question) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(roomMeta(roomCode)))) {
            throw new IllegalStateException("Room does not exist");
        }

        validateHost(roomCode, hostId);

        Boolean firstTime = redisTemplate.opsForSet().add(
                roomSentQuestions(roomCode),
                question.getId().toString()
        ) == 1;

        if (!firstTime) {
            throw new IllegalStateException(
                    "Question already sent. Move to next question."
            );
        }

        String questionKey = roomQuestion(roomCode);

        redisTemplate.opsForHash().put(questionKey, "id",
                String.valueOf(question.getId()));

        redisTemplate.opsForHash().put(questionKey, "type",
                question.getType().name());

        redisTemplate.opsForHash().put(questionKey, "text",
                question.getText());

        if (question.getOptionsJson() != null) {
            redisTemplate.opsForHash().put(questionKey, "options",
                    question.getOptionsJson());
        }

        redisTemplate.opsForHash().put(questionKey, "endTime",
                question.getEndTime().toString());

        redisTemplate.opsForHash().put(questionKey, "points",
                String.valueOf(question.getPoints()));

        redisTemplate.opsForHash().put(questionKey, "timeLimitSeconds",
                String.valueOf(question.getTimeLimitSeconds()));

        redisTemplate.opsForValue().set(
                "room:" + roomCode + ":question:answer",
                String.join(",", question.getCorrectAnswer())
        );

        redisTemplate.opsForHash().put(
                roomQuestion(roomCode),
                "questionKey",
                UUID.randomUUID().toString()
        );


        quizSocketService.broadcastQuestion(roomCode, QuestionViewDTO.from(question));

        // 🔥 SCHEDULE EVALUATION
        scheduleEvaluation(roomCode, question);
    }


    private void scheduleEvaluation(String roomCode, QuestionDTO question) {
        long delayMs =
                question.getEndTime().toEpochMilli()
                        - Instant.now().toEpochMilli();

        if (delayMs < 0) return;

        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
                .execute(() -> evaluateQuestion(roomCode, question));
    }

    private void evaluateQuestion(String roomCode, QuestionDTO question) {
        String answerKey = "room:" + roomCode + ":answers:" + question.getId();
        String correctKey = "room:" + roomCode + ":question:answer";
        String scoreKey = "room:" + roomCode + ":scores";

        Set<String> correctAnswers =
                Set.of(redisTemplate.opsForValue().get(correctKey).split(","));

        Map<Object, Object> answers =
                redisTemplate.opsForHash().entries(answerKey);

        answers.forEach((userIdObj, valueObj) -> {
            String userId = userIdObj.toString();
            String submittedAnswer = valueObj.toString().split("\\|")[0];

            Set<String> submittedSet = Arrays.stream(submittedAnswer.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            if (submittedSet.contains(submittedAnswer)) {
                redisTemplate.opsForHash().increment(
                        scoreKey,
                        userId,
                        question.getPoints()
                );
            }
        });

        // 🔥 SORT & BROADCAST LEADERBOARD
        Map<String, Integer> leaderboard = getSortedLeaderboard(scoreKey);
        quizSocketService.broadcastLeaderboard(roomCode, leaderboard);

        redisTemplate.delete(roomQuestion(roomCode));
        redisTemplate.delete(answerKey);
        redisTemplate.delete(correctKey);

        // ✅ NOTIFY CLIENTS QUESTION ENDED
        quizSocketService.broadcastQuestionEnded(roomCode);
    }


    private Map<String, Integer> getSortedLeaderboard(String scoreKey) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(scoreKey);

        return raw.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> Integer.parseInt(e.getValue().toString())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    public void submitAnswer(String roomCode, String questionId, Long userId, String answer) {
        validateUserInRoom(roomCode, userId);
        validateQuestionTime(roomCode);

        String status = (String) redisTemplate.opsForHash()
                .get(roomMeta(roomCode), "status");

        if (!"ACTIVE".equals(status)) {
            throw new IllegalStateException("Room not active");
        }

        String answerKey = "room:" + roomCode + ":answers:" + questionId;
        String value = answer + "|" + Instant.now().toString();
        Boolean first = redisTemplate.opsForHash()
                .putIfAbsent(answerKey, userId.toString(), value);

        if (Boolean.FALSE.equals(first)) {
            throw new IllegalStateException("Answer already submitted");
        }
    }

    // Server-only method. Never exposed to clients.
    public void updateScoreInternal(String roomCode, Long userId, int score) {
        String scoreKey = "room:" + roomCode + ":scores";

        redisTemplate.opsForHash().increment(
                scoreKey,
                userId.toString(),
                score
        );
    }

    public Map<Object, Object> getLeaderboard(String roomCode) {
        String scoreKey = "room:" + roomCode + ":scores";
        return redisTemplate.opsForHash().entries(scoreKey);
    }

    private void validateUserInRoom(String roomCode, Long userId) {
        Boolean member = redisTemplate.opsForSet()
                .isMember(roomUsers(roomCode), userId.toString());

        if (Boolean.FALSE.equals(member)) {
            throw new SecurityException("User not part of room");
        }
    }

    private void persistAttemptsFromRedis(String roomCode) {

        Map<Object, Object> scores =
                redisTemplate.opsForHash().entries("room:" + roomCode + ":scores");

        scores.forEach((userIdObj, scoreObj) -> {

            Long userId = Long.valueOf(userIdObj.toString());
            int score = Integer.parseInt(scoreObj.toString());

            QuizAttempt attempt = new QuizAttempt();

            User user = new User();
            user.setId(userId); // proxy reference

            attempt.setUser(user);
            attempt.setRoomCode(roomCode);
            attempt.setFinalScore(score);
            attempt.setCompletedAt(Instant.now());

            quizAttemptRepository.save(attempt);
        });
    }

    private void cleanupRoom(String roomCode) {
        ScanOptions options = ScanOptions.scanOptions()
                .match("room:" + roomCode + "*")
                .count(100)
                .build();

        var connectionFactory = redisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            throw new IllegalStateException("Redis connection factory not available");
        }

        try (var connection = connectionFactory.getConnection();
             Cursor<byte[]> cursor = connection.scan(options)) {

            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                connection.keyCommands().del(key);
            }
        }
    }


    public List<Map<String, Object>> getPlayers(String roomCode) {
        Set<String> userIds = redisTemplate.opsForSet()
                .members(roomUsers(roomCode));

        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return userIds.stream()
                .map(id -> {
                    User user = userRepository.findById(Long.valueOf(id))
                            .orElseThrow(() -> new IllegalStateException("User not found"));

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("name", user.getDisplayName());
                    map.put("score", 0); // initial score, real scores come via LEADERBOARD

                    return map;
                })
                .collect(Collectors.toList());
    }


    private void validateHost(String roomCode, Long userId) {
        String hostId = (String) redisTemplate.opsForHash()
                .get(roomMeta(roomCode), "hostId");

        if (!userId.toString().equals(hostId)) {
            throw new SecurityException("Only host allowed");
        }
    }

    private void validateQuestionTime(String roomCode) {
        Object endTimeObj = redisTemplate.opsForHash()
                .get(roomQuestion(roomCode), "endTime");

        if (endTimeObj == null) {
            throw new IllegalStateException("No active question");
        }

        if (Instant.now().isAfter(Instant.parse(endTimeObj.toString()))) {
            throw new IllegalStateException("Answer window closed");
        }
    }

    public boolean isHost(String roomCode, Long userId) {
        String hostId = (String) redisTemplate.opsForHash()
                .get(roomMeta(roomCode), "hostId");
        return userId.toString().equals(hostId);
    }

    public void removeUser(String roomCode, Long userId) {
        redisTemplate.opsForSet()
                .remove(roomUsers(roomCode), userId.toString());
    }

    public RoomStateResponse getRoomState(String roomCode, Long userId) {
        // 1️⃣ Validate user is in room
        validateUserInRoom(roomCode, userId);

        // 2️⃣ Players snapshot
        List<Map<String, Object>> players = getPlayers(roomCode);

        // 3️⃣ Leaderboard snapshot (FIX IS HERE)
        Map<Object, Object> rawScores = getLeaderboard(roomCode);

        Map<String, Integer> leaderboard = new HashMap<>();
        rawScores.forEach((k, v) -> {
            leaderboard.put(
                    k.toString(),
                    Integer.parseInt(v.toString())
            );
        });

        // 4️⃣ Current question snapshot
        Map<String, Object> currentQuestion = getCurrentQuestion(roomCode);

        return new RoomStateResponse(players, leaderboard, currentQuestion);
    }

    private Map<String, Object> getCurrentQuestion(String roomCode) {
        Map<Object, Object> raw =
                redisTemplate.opsForHash().entries(roomQuestion(roomCode));

        if (raw == null || raw.isEmpty()) {
            return null;
        }

        Map<String, Object> question = new HashMap<>();
        raw.forEach((k, v) -> question.put(k.toString(), v));
        return question;
    }


    private String roomMeta(String code) {
        return "room:" + code + ":meta";
    }

    private String roomUsers(String code) {
        return "room:" + code + ":users";
    }

    private String roomQuestion(String code) {
        return "room:" + code + ":question";
    }

    private String roomSentQuestions(String code) {
        return "room:" + code + ":sentQuestions";
    }


}

