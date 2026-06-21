/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the product-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.product_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class ProductServiceCorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns(
                    "http://localhost:8080", "http://localhost:8083"
//                    "http://localhost:[*]",
//                    "http://127.0.0.1:[*]"
//                   "http://host.docker.internal:",
//                "http://api-gateway:[*]",
//                    "http://172.18.*.*.*:[*]", //new //internal docker network range
//                    "http://product-service:[*]"
            ) // Allow all origins
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH") // Allow specific methods
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true); // Allow credentials
  }
}
