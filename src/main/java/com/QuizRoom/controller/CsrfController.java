package com.QuizRoom.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/csrf-token")
    public CsrfToken csrfToken(CsrfToken token) {
        // Spring automatically generates and returns CSRF token
        // Token is also set in cookie by CookieCsrfTokenRepository
        return token;
    }
}
