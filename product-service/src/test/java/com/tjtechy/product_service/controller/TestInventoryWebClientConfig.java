//NOTE: For now, it is not working with the test.
package com.tjtechy.product_service.controller;

import com.tjtechy.product_service.config.InventoryServiceConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class TestInventoryWebClientConfig {

  @Bean(name = "inventoryServiceWebClient")
  public WebClient inventoryServiceWebClient(InventoryServiceConfig inventoryServiceConfig) {
    return WebClient.builder()
        .baseUrl(inventoryServiceConfig.getBaseUrl()) // Will be injected during tests
        .build();
  }
}
