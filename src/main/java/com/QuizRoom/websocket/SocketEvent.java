package com.QuizRoom.websocket;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SocketEvent<T> {
    private SocketEventType type;
    private T payload;
}
