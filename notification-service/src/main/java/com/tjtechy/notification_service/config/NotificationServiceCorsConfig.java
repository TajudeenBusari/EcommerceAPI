/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the notification-service of the Ecommerce Microservices project.
 */
package com.tjtechy.notification_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class NotificationServiceCorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:8080", "http://localhost:8085")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
