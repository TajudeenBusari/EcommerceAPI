/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
// @ActiveProfiles("test") // Uncomment this line if you have a specific test profile
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cloud.config.enabled=false", // Disable Spring Cloud Config
        "eureka.client.enabled=false", // Disable Eureka Client
        "spring.cloud.loadbalancer.enabled=false", // Disable LoadBalancer
})
class InventoryServiceApplicationTest {
  @Test
  void contextLoads() {
    // This test will simply check if the Spring application context loads successfully
  }
}
