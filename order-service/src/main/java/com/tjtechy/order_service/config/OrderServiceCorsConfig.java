/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the order-service module of the EcommerceMicroservices project.
 */

package com.tjtechy.order_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderServiceCorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns(
                    "http://localhost:8080", "http://localhost:8082"
            ) // Allow all origins
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allow specific methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true); // Allow credentials
  }
}
