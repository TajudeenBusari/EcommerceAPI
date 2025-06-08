/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tjtechy.ProductDto;
import com.tjtechy.RedisCacheConfig;
import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderItemDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.cloud.config.client.ConfigServerBootstrapper;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // Exclude caching to avoid issues with Redis
@AutoConfigureWireMock(port = 0) // Use a random port for WireMock
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //prevents the test from using an embedded database
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.application.name=order-service",
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:tc:postgresql:15.2:///testdb",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "redis.enabled=false", //disable Redis cache
//        "product-service.base-url=http://localhost:${wiremock.server.port}/api/v1",
//        "inventory-service.base-url=http://localhost:${wiremock.server.port}/api/v1",
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.loadbalancer.enabled=false",
})
@ImportAutoConfiguration(exclude = {
        // Exclude any auto-configuration classes that are not needed for the test
        RedisCacheConfig.class,
        EurekaClientAutoConfiguration.class,
        ConfigServerBootstrapper.class
})
@WireMockTest


public class OrderServiceControllerIntegrationTest {

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WebTestClient webClient;

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");
  private static String wireMockBaseUrl;


  @DynamicPropertySource
  public static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);

    //Dynamically set the product and inventory service base URLs to WireMock's port
    registry.add("product-service.base-url", () -> wireMockBaseUrl + "/api/v1");
    registry.add("inventory-service.base-url", () -> wireMockBaseUrl + "/api/v1");

  }

  @BeforeAll
  public static void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
    postgreSQLContainer.start();

    wireMockBaseUrl = "http://localhost:" + wireMockRuntimeInfo.getHttpPort();
  }

  @AfterAll
  public static void tearDown() {
    if (postgreSQLContainer.isRunning()) {
      postgreSQLContainer.stop();
    }

  }

  // Add your test methods here, using MockMvc or WebTestClient to test the OrderController
  @Test
  void testCreateOrderSuccess(WireMockRuntimeInfo wireMockRuntimeInfo) throws Exception {
    var productServiceBaseUrl = wireMockRuntimeInfo.getHttpBaseUrl() + "/api/v1/product";
    var inventoryServiceBaseUrl = wireMockRuntimeInfo.getHttpBaseUrl() + "/api/v1/inventory";
    System.out.println(productServiceBaseUrl);
    System.out.println(inventoryServiceBaseUrl);
    //Arrange
    var productId1 = UUID.randomUUID();
    var productId2 = UUID.randomUUID();

    //prepare a CreateOrderDto with sample data
    List<OrderItemDto> orderItems = List.of(
        new OrderItemDto(productId1, "Product1", 90),
        new OrderItemDto(productId2, "Product2", 40)
    );
    var createOrderDto = new CreateOrderDto(
            "John Doe " + System.currentTimeMillis(),
            "john@email.com",
            "123 Main St, City, Country",
            orderItems
    );

    ProductDto productDto1 = new ProductDto(productId1,
            "Product1",
            "Category1",
            "Description1",
            100,
            10,
            LocalDate.now().plusDays(30),
            BigDecimal.valueOf(10.99));
    ProductDto productDto2 = new ProductDto(productId2,
            "Product2",
            "Category2",
            "Description2",
            50,
            5,
            LocalDate.now().plusDays(60),
            BigDecimal.valueOf(20.99));

    //STUB the product service response using WireMock for productId1
    stubFor(get(urlEqualTo("/api/v1/product/" + productId1))
    .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(new Result("Get One Success", true, productDto1, StatusCode.SUCCESS)))));

    //STUB the product service response using WireMock for productId2
    stubFor(get(urlEqualTo("/api/v1/product/" + productId2))
    .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(new Result("Get One Success", true, productDto2, StatusCode.SUCCESS)))));

    //Mock inventory service responses
    stubFor(patch(urlEqualTo("/api/v1/inventory/internal/deduct-reactive"))
    .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS)))));

    //Act and Assert
    webClient.post()
            .uri(baseUrl+ "/order/reactive")
            //.uri("http://localhost:8082/api/v1/order/reactive")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(createOrderDto)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .value(result -> {
                // Assert the response
                assert result.isFlag();
                assert "Order created successfully".equals(result.getMessage());
                assert result.getData() != null;
                assert result.getCode() == StatusCode.SUCCESS;
            });
  }

}
