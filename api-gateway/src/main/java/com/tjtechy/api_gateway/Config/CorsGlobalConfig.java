/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.api_gateway.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

//TODO: FIX: Request cannot be made directly to the services from the gateway may be because of CORS policy
//when i make a get all notification request on the browser with: http://192.168.56.1:8085/api/v1/notification,
// it returns the response in xml format

@Configuration
public class CorsGlobalConfig {
  //allow cors for all endpoints
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]",
            "http://host.docker.internal:[*]",
            "http://172.*.*.*:[*]",
            "http://192.168.*.*:[*]",
            "http://192.168.56.1",
            "http://10.*.*.*:[*]",
            "https://docs.swagger.io",
            "http://notification-service:[*]"
    ));
    config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Origin",
            "Accept",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"));
    config.setAllowedMethods(Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
  }



}
