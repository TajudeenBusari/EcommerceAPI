/**
 * Not in use currently.
 */
package com.tjtechy;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestConfig {
  @Bean
  public WebClient productServiceWebClient() {
    return WebClient.builder()
        .baseUrl("http://localhost:8083/api/v1") // Replace with the actual URL of your product service
        .build();
  }
}
