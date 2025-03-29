/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The Product service will be called using this web client.
 */
@Configuration
public class WebClientConfiguration {

  @Bean
  @LoadBalanced
  public WebClient.Builder getWebClientBuilder() {
    return WebClient.builder();
  }
}
