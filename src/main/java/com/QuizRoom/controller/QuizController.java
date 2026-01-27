package com.QuizRoom.controller;

import com.QuizRoom.dto.QuestionDTO;
import com.QuizRoom.dto.QuestionViewDTO;
import com.QuizRoom.security.CustomerUserDetails;
import com.QuizRoom.service.AnswerEvaluationService;
import com.QuizRoom.service.QuizSocketService;
import com.QuizRoom.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


@RestController
@RequestMapping("/api/rooms/{roomCode}/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final RoomService roomService;
    private final QuizSocketService quizSocketService;
    private final AnswerEvaluationService answerEvaluationService;

    // 5. Broadcast question
    @PostMapping("/question")
    public void broadcastQuestion(
            @PathVariable String roomCode,
            @RequestBody QuestionDTO question,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        if (!roomService.isHost(roomCode, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        roomService.publishQuestion(roomCode, user.getId(), question);
        quizSocketService.broadcastQuestion(roomCode, QuestionViewDTO.from(question));
    }

    // 6. End question + leaderboard
    @PostMapping("/question/{questionId}/end")
    public void endQuestion(
            @PathVariable String roomCode,
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomerUserDetails user

    ) {
        if (!roomService.isHost(roomCode, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        answerEvaluationService.evaluateQuestion(roomCode, questionId);
        quizSocketService.broadcastLeaderboard(
                roomCode,
                roomService.getLeaderboard(roomCode)
        );
    }


    @PostMapping("/end")
    public void endQuiz(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails host
    ) {
        roomService.endRoom(roomCode, host.getId());

        quizSocketService.broadcastQuizEnded(
                roomCode,
                roomService.getLeaderboard(roomCode)
        );
    }

    @GetMapping("/results")
    public Map<Object, Object> getFinalResults(
            @PathVariable String roomCode
    ) {
        return roomService.getLeaderboard(roomCode);
    }

}
