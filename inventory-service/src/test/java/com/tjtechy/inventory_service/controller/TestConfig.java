/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
//not currently used in the test class because the application context that provides this bean is already loaded when the test is run
@TestConfiguration
public class TestConfig {
  @Bean
  WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
