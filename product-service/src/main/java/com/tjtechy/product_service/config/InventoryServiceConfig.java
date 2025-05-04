package com.tjtechy.product_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryServiceConfig {
  /**
   * This class is used to configure the inventory service.
   * It contains the base URL of the inventory service.
   */
  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
