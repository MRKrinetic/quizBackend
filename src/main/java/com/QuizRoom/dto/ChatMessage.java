package com.QuizRoom.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatMessage {

    private Long userId;
    private String username;
    private String message;
    private Instant sentAt;

    public ChatMessage() {
    }

    public ChatMessage(Long userId, String username, String message, Instant sentAt) {
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.sentAt = sentAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
