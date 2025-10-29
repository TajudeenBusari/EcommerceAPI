/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
  /**
   * Password encoder bean using BCrypt
   * @return PasswordEncoder
   * note: BCrypt is a strong hashing algorithm that is widely used for securely storing passwords.
   * This bean is needed when using the PasswordEncoder interface in the application.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Security filter chain configuration
   * Once there is a spring security dependency, by default, all endpoints are secured.
   * So make a state change like a POST, PUT, DELETE request to any endpoint will require CSRF token.
   * For simplicity, we are disabling CSRF here.
   * @param http
   * @return
   * @throws Exception
   */
  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/api/v1/user/register").permitAll()
                    .anyExchange()
                    .permitAll()
            )
            .build();
  }
}
