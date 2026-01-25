package com.QuizRoom.controller;

import com.QuizRoom.entity.User;
import com.QuizRoom.repository.UserRepository;
import com.QuizRoom.security.CustomerUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        String email = null;

        if (principal instanceof CustomerUserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (principal instanceof String principalEmail) {
            email = principalEmail;
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        // Return only necessary user info (not internal fields like googleSub)
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : "",
                "picture", user.getPicture() != null ? user.getPicture() : ""
        ));
    }
}
