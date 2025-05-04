//NOTE: For now, it is not working with the test.
package com.tjtechy.order_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestConfig {
  @Value("${product-service.url}")
  private String productServiceUrl;

  @Bean
  public WebClient productServiceWebClient() {
    return WebClient.builder()
        .baseUrl(productServiceUrl)
        .build();
  }
}
