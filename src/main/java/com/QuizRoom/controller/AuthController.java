package com.QuizRoom.controller;

import com.QuizRoom.entity.User;
import com.QuizRoom.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/auth/me")
    public User me(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) return null;

        String sub = oAuth2User.getAttribute("sub");
        return userRepository.findByGoogleSub(sub).orElse(null);
    }
}
