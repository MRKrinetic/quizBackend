package com.QuizRoom.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public static CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
                Arrays.asList("https://quizbackend-1-uzq8.onrender.com", "https://quiz-room-mu.vercel.app/")
        );
        
        config.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        
        // Explicit headers required when allowCredentials = true
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-XSRF-TOKEN",
                "X-Requested-With"
        ));
        
        // Expose headers that frontend needs to read
        config.setExposedHeaders(Arrays.asList(
                "X-XSRF-TOKEN"
        ));
        
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
