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

    private String topic(String roomCode) {
        return "/topic/room/" + roomCode;
    }

    public void broadcastQuestion(String roomCode, QuestionViewDTO question) {
        messagingTemplate.convertAndSend(
                topic(roomCode),
                new SocketEvent<>("QUESTION", question)
        );
    }

    public void broadcastLeaderboard(String roomCode, Map<Object, Object> leaderboard) {
        messagingTemplate.convertAndSend(
                topic(roomCode),
                new SocketEvent<>("LEADERBOARD", leaderboard)
        );
    }

    public void broadcastRoomEnded(String roomCode) {
        messagingTemplate.convertAndSend(
                topic(roomCode),
                new SocketEvent<>("ROOM_ENDED", "Quiz ended")
        );
    }

    public void broadcastQuizEnded(String roomCode, Object payload) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<>(SocketEventType.QUIZ_ENDED.name(), payload)
        );
    }
}
