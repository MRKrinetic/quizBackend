package com.QuizRoom.dto;

import com.QuizRoom.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private Long id;                 // Question ID (DB or generated)
    private QuestionType type;        // MCQ / MSQ / NAT
    private String text;              // Question text
    private String optionsJson;       // JSON string for options (MCQ/MSQ)
    private List<String> correctAnswer;     // Server-only
    private Integer points;           // Score weight
    private String questionKey;
    private Instant endTime;          // Answer deadline
    private Integer timeLimitSeconds;

}
