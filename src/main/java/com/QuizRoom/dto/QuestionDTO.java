package com.QuizRoom.dto;

import com.QuizRoom.entity.QuestionType;
import lombok.Data;

import java.time.Instant;

@Data
public class QuestionDTO {

    private Long id;                 // Question ID (DB or generated)
    private QuestionType type;        // MCQ / MSQ / NAT
    private String text;              // Question text
    private String optionsJson;       // JSON string for options (MCQ/MSQ)
    private String correctAnswer;     // Server-only
    private Integer points;           // Score weight
    private Instant endTime;          // Answer deadline
    private Integer timeLimitSeconds;

    public QuestionDTO() {
    }

    public QuestionDTO(
            Long id,
            QuestionType type,
            String text,
            String optionsJson,
            String correctAnswer,
            Integer points,
            Instant endTime,
            Integer timeLimitSeconds
    ) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.optionsJson = optionsJson;
        this.correctAnswer = correctAnswer;
        this.points = points;
        this.endTime = endTime;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

}
