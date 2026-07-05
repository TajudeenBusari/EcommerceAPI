/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the User Service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    //Allow both Gateway and direct access
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:8086"));

    //Allow all HTTP methods
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    //Allow all headers
    configuration.setAllowedHeaders(List.of("*"));

    //Allow credentials (cookies, authorization headers, etc.)
    configuration.setAllowCredentials(true);

    //Cache preflight response for 3600 seconds
    configuration.setMaxAge(3600L);

    //Apply this configuration to all paths
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
