package com.QuizRoom.websocket;

import com.QuizRoom.service.QuizSocketService;
import com.QuizRoom.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

    private final RoomService roomService;
    private final QuizSocketService socketService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        String roomCode = (String) event.getMessage()
                .getHeaders()
                .get("roomCode");

        Long userId = (Long) event.getMessage()
                .getHeaders()
                .get("userId");

        if (roomCode == null || userId == null) return;

        if (roomService.isHost(roomCode, userId)) {
            roomService.endRoom(roomCode, userId);
            socketService.broadcastRoomEnded(roomCode);
        } else {
            roomService.removeUser(roomCode, userId);
        }
    }
}
