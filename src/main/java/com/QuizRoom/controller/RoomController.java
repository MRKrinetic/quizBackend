package com.QuizRoom.controller;

import com.QuizRoom.dto.RoomStateResponse;
import com.QuizRoom.security.CustomerUserDetails;
import com.QuizRoom.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 1. Create room
    @PostMapping("/create")
    public String createRoom(@AuthenticationPrincipal CustomerUserDetails host) {
        if (host == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return roomService.createRoom(host.getId());
    }

    // 2. Join room
    @PostMapping("/{roomCode}/join")
    public ResponseEntity<Void> joinRoom(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        if(user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        roomService.joinRoom(roomCode, user.getId());
        return ResponseEntity.ok().build();
    }

    // 3. Host validation (CRITICAL)
    @GetMapping("/{roomCode}/validate-host")
    public void validateHost(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        if (!roomService.isHost(roomCode, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    // 4. End room + persist results
    @PostMapping("/{roomCode}/end")
    public void endRoom(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails host
    ) {
        System.out.println("🔥 CONTROLLER /end HIT for room " + roomCode);
        roomService.endRoom(roomCode, host.getId());
    }

    @GetMapping("/{roomCode}/players")
    public List<Map<String, Object>> getPlayers(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return roomService.getPlayers(roomCode);
    }

    @GetMapping("/{roomCode}/state")
    public RoomStateResponse getRoomState(
            @PathVariable String roomCode,
            @AuthenticationPrincipal CustomerUserDetails user
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return roomService.getRoomState(roomCode, user.getId());
    }

}
