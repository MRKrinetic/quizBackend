package com.QuizRoom.controller;

import com.QuizRoom.dto.ChatMessage;
import com.QuizRoom.websocket.SocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomCode}/chat")
    public void chat(
            @DestinationVariable String roomCode,
            ChatMessage message
    ) {
        message.setSentAt(Instant.now());

        // Persist chat (simple string storage)
        redisTemplate.opsForList().rightPush(
                "room:" + roomCode + ":chat",
                message.getUsername() + ": " + message.getMessage()
        );

        // Broadcast to all clients
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode,
                new SocketEvent<>("CHAT", message)
        );
    }
}
