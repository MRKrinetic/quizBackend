package com.QuizRoom.service;

import com.QuizRoom.dto.QuestionViewDTO;
import com.QuizRoom.websocket.SocketEvent;
import com.QuizRoom.websocket.SocketEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastQuestion(String roomCode, QuestionViewDTO question) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<QuestionViewDTO>(SocketEventType.QUESTION, question )
        );
    }

    public void broadcastLeaderboard(String roomCode, Map<?, ?> leaderboard) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<Map<?, ?>>(
                        SocketEventType.LEADERBOARD,
                        leaderboard
                )
        );
    }

    public void broadcastQuizEnded(String roomCode, Map<?, ?> leaderboard) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<Map<?, ?>>(
                        SocketEventType.QUIZ_ENDED,
                        leaderboard
                )
        );
    }

    public void broadcastPlayerJoined(String roomCode, Map<String, Object> payload) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<Map<String, Object>>(
                        SocketEventType.PLAYER_JOINED,
                        payload
                )
        );
    }

    public void broadcastRoomEnded(String roomCode) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<Void>(
                        SocketEventType.ROOM_ENDED,
                        null
                )
        );
    }

    public void broadcastQuestionEnded(String roomCode) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<Void>(SocketEventType.QUESTION_ENDED, null)
        );
    }

}
