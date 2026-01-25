package com.QuizRoom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnswerEvaluationService {

    private final StringRedisTemplate redisTemplate;
    private final RoomService roomService;

    public void evaluateQuestion(String roomCode, Long questionId) {

        String answerKey = "room:" + roomCode + ":answers:" + questionId;
        String questionKey = "room:" + roomCode + ":question";
        String correctAnswerKey = "room:" + roomCode + ":question:answer";

        String correctAnswer = redisTemplate.opsForValue().get(correctAnswerKey);
        if (correctAnswer == null) {
            throw new IllegalStateException("Correct answer not found");
        }

        String endTimeStr = (String) redisTemplate.opsForHash().get(questionKey, "endTime");
        String timeLimitStr = (String) redisTemplate.opsForHash().get(questionKey, "timeLimitSeconds");
        String pointsStr = (String) redisTemplate.opsForHash().get(questionKey, "points");

        if (endTimeStr == null || timeLimitStr == null || pointsStr == null) {
            throw new IllegalStateException("Question metadata missing");
        }

        Instant endTime = Instant.parse(endTimeStr);
        long totalTimeSeconds = Long.parseLong(timeLimitStr);
        int maxPoints = Integer.parseInt(pointsStr);

        Map<Object, Object> submittedAnswers =
                redisTemplate.opsForHash().entries(answerKey);

        for (Map.Entry<Object, Object> entry : submittedAnswers.entrySet()) {

            Long userId = Long.valueOf(entry.getKey().toString());

            String value = entry.getValue().toString();
            String[] parts = value.split("\\|");
            if (parts.length != 2) continue;

            String userAnswer = parts[0];
            Instant submittedAt = Instant.parse(parts[1]);

            long remainingSeconds = Math.max(
                    0,
                    endTime.getEpochSecond() - submittedAt.getEpochSecond()
            );

            int score = calculateScore(
                    userAnswer,
                    correctAnswer,
                    maxPoints,
                    remainingSeconds,
                    totalTimeSeconds
            );

            if (score > 0) {
                roomService.updateScoreInternal(roomCode, userId, score);
            }
        }
    }


    private int calculateScore(
            String userAnswer,
            String correctAnswer,
            int maxPoints,
            long remainingSeconds,
            long totalTimeSeconds
    ) {
        if (!userAnswer.equalsIgnoreCase(correctAnswer)) {
            return 0;
        }
        if (totalTimeSeconds <= 0) {
            return maxPoints;
        }

        double timeFactor = (double) remainingSeconds / totalTimeSeconds;
        double bonus = timeFactor * maxPoints;

        return (int) Math.round(maxPoints + bonus);
    }
}
