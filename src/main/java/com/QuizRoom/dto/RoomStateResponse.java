package com.QuizRoom.dto;

import java.util.List;
import java.util.Map;

public record RoomStateResponse(
        List<Map<String, Object>> players,
        Map<String, Integer> leaderboard,
        Map<String, Object> currentQuestion
) {}
