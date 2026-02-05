package com.QuizRoom.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.QuizRoom.security.JwtUtil;
import com.QuizRoom.entity.User;
import com.QuizRoom.repository.UserRepository;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String googleSub = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = userRepository.findByGoogleSub(googleSub)
                .orElse(new User());

        user.setGoogleSub(googleSub);
        user.setEmail(email);
        user.setPicture(picture);

        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            user.setDisplayName(name);
        }

        userRepository.save(user);

        String jwt = jwtUtil.generateToken(email);

        ResponseCookie cookie = ResponseCookie.from("JWT", jwt)
                .httpOnly(true)
                .secure(true)       // false for development (localhost)
                .sameSite("None")     // Lax for development, use None with secure=true in production
                .path("/")
                .domain("quizbackend-acm9.onrender.com") 
                .maxAge(24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Redirect to FRONTEND
        response.sendRedirect("https://quiz-room-mu.vercel.app");
    }
}
