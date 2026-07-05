/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.inventory_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class configures CORS settings for the Inventory Service.
 * It allows cross-origin requests from specified origins and methods.
 * Because the Inventory-service is built using Spring WebMvc as against WebFlux, we implement WebMvcConfigurer.
 * NOTE: ALMOST SAME CORS IS IMPLEMENTED IN THE API GATEWAY yml file, SO THAT
 * ALL MICROSERVICES CAN INHERIT THE CORS SETTINGS FROM THE API GATEWAY AND REQUEST CAN BE
 * MADE DIRECTLY FROM THE API GATEWAY IN SWAGGER UI.
 */
@Configuration
public class InventoryServiceCorsConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            //.allowedOriginPatterns("*") // Allow all origins
            .allowedOriginPatterns(
                    "http://localhost:[*]",
                    "http://127.0.0.1:[*]",
                    "http://host.docker.internal:[*]",
                    "http://api-gateway:[*]",
                    "http://172.18.*.*.*:[*]" //new //internal docker network range
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allow specific methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true); // Allow credentials
  }
}
