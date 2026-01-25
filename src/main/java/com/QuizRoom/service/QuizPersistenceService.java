package com.QuizRoom.service;

import com.QuizRoom.entity.Quiz;
import com.QuizRoom.entity.QuizAttempt;
import com.QuizRoom.entity.User;
import com.QuizRoom.repository.QuizAttemptRepository;
import com.QuizRoom.repository.QuizRepository;
import com.QuizRoom.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizPersistenceService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void persistResults(String roomCode, Long quizId) {

        String scoreKey = "room:" + roomCode + ":scores";
        Map<Object, Object> scores =
                redisTemplate.opsForHash().entries(scoreKey);

        if (scores.isEmpty()) return;

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalStateException("Quiz not found"));

        List<Map.Entry<Object, Object>> sorted =
                scores.entrySet().stream()
                        .sorted((a, b) ->
                                Integer.compare(
                                        Integer.parseInt(b.getValue().toString()),
                                        Integer.parseInt(a.getValue().toString())
                                ))
                        .toList();

        int rank = 1;
        Instant now = Instant.now();

        for (Map.Entry<Object, Object> entry : sorted) {

            Long userId = Long.valueOf(entry.getKey().toString());
            int score = Integer.parseInt(entry.getValue().toString());

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalStateException("User not found: " + userId)
                    );

            QuizAttempt attempt = new QuizAttempt();
            attempt.setUser(user);
            attempt.setQuiz(quiz);
            attempt.setRoomCode(roomCode);
            attempt.setFinalScore(score);
            attempt.setRank(rank++);
            attempt.setStartedAt(quiz.getCreatedAt()); // or room createdAt
            attempt.setCompletedAt(now);

            quizAttemptRepository.save(attempt);
        }
    }
}
