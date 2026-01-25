package com.QuizRoom.room;

import lombok.Getter;

@Getter
public class PlayerState {

    private final Long userId;
    private final String username;
    private int score = 0;

    public PlayerState(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public void addScore(int delta) {
        this.score += delta;
    }
}
