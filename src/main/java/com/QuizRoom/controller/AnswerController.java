package com.QuizRoom.controller;

import com.QuizRoom.security.CustomerUserDetails;
import com.QuizRoom.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/{roomCode}/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final RoomService roomService;

    // 7. Submit answer
    @PostMapping("/{questionId}")
    public void submitAnswer(
            @PathVariable String roomCode,
            @PathVariable String questionId,
            @RequestBody String answer,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        roomService.submitAnswer(roomCode, questionId, user.getId(), answer);
    }
}
