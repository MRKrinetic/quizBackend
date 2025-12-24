package com.QuizRoom.config;

import com.QuizRoom.entity.User;
import com.QuizRoom.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String sub = oauthUser.getAttribute("sub");
        String name = oauthUser.getAttribute("name");
        String email = oauthUser.getAttribute("email");
        String picture = oauthUser.getAttribute("picture");

        User user = userRepository.findByGoogleSub(sub)
                .orElseGet(() -> {
                    User u = new User();
                    u.setGoogleSub(sub);
                    u.setEmail(email);
                    u.setPicture(picture);
                    u.setDisplayName(name); // ✅ default name
                    return u;
                });

        userRepository.save(user);

        response.sendRedirect("http://localhost:8080");
    }
}
