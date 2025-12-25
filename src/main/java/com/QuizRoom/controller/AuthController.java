package com.QuizRoom.controller;

import com.QuizRoom.entity.User;
import com.QuizRoom.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/auth/me")
    public User me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // After JWT authentication, the principal is the email string
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email).orElse(null);
    }
}
