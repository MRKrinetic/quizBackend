package com.QuizRoom.dto;

import com.QuizRoom.entity.QuestionType;
import lombok.Data;

import java.time.Instant;

@Data
public class QuestionViewDTO {

    private Long id;
    private QuestionType type;
    private String text;
    private String optionsJson;
    private Instant endTime;

    public QuestionViewDTO() {
    }

    public QuestionViewDTO(
            Long id,
            QuestionType type,
            String text,
            String optionsJson,
            Instant endTime
    ) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.optionsJson = optionsJson;
        this.endTime = endTime;
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

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public static QuestionViewDTO from(QuestionDTO dto) {
        QuestionViewDTO view = new QuestionViewDTO();
        view.setId(dto.getId());
        view.setType(dto.getType());
        view.setText(dto.getText());
        view.setOptionsJson(dto.getOptionsJson());
        view.setEndTime(dto.getEndTime());
        return view;
    }
}
