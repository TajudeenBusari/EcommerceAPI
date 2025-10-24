/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Needed to enable load balancing for WebClient in order-service.
 * This allows the order-service to communicate with other services
 */
@Configuration
public class WebClientConfiguration {

  /**
   * This bean provides a load-balanced WebClient.Builder
   * that can be used to create WebClient instances for making HTTP requests
   * to other services in a load-balanced manner.
   * The ObservationRegistry is used to enable observability features
   * to trace all requests to other services.
   * When creating orders, this service calls the product-service to check if product
   * exists and the inventory-service to deduct the inventory.
   * @param observationRegistry
   * @return
   */
  @Bean
  @LoadBalanced
  public WebClient.Builder getWebClientBuilder(ObservationRegistry observationRegistry) {
    return WebClient.builder().observationRegistry(observationRegistry);
  }

  //default WebClient bean
  @Bean
  public WebClient getWebClient(WebClient.Builder builder) {
    return WebClient.builder().build();
  }
}
