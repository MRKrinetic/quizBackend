package com.QuizRoom.dto;

import com.QuizRoom.entity.QuestionType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionViewDTO {

    private Long id;
    private QuestionType type;
    private String text;
    private List<String> options;
    private Instant endTime;
    private String questionKey;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static QuestionViewDTO from(QuestionDTO q) {
        List<String> options = List.of();

        if (q.getOptionsJson() != null && !q.getOptionsJson().isBlank()) {
            options = readOptions(q.getOptionsJson());
        }

        return new QuestionViewDTO(
                q.getId(),
                q.getType(),
                q.getText(),
                options,
                q.getEndTime(),   // ✅ MUST exist in QuestionDTO
                q.getQuestionKey()
        );
    }

    private static List<String> readOptions(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Invalid optionsJson", e);
        }
    }
}
