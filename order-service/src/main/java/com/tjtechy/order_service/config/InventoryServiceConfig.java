package com.tjtechy.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConfigurationProperties(prefix = "inventory.service")
public class InventoryServiceConfig {
 @Value("${api.endpoint.base-url}")
  //@Value("${inventory.service.base-url}")
  private String baseUrl;
  public String getBaseUrl() {
    return baseUrl;
  }
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
