
/*
 * Copyright © 2025  
 * @Author = TJTechy (Tajudeen Busari)  
 * @Version = 1.0  
 * This file is part user-service test module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Test security configuration that disables security for testing purposes.
 * We have a similar config in the security module, but in the future we may need to
 * enable that in that module (security) and disable it here for testing only.
 */
@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                    .anyExchange().permitAll()
            )
            .build();
  }
}
