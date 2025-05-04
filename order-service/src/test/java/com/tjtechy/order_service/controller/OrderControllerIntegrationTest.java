/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderDto;
import com.tjtechy.order_service.entity.dto.OrderItemDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import redis.embedded.RedisServer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cloud.config.enabled=false",//disable spring cloud config
        "eureka.client.enabled=false",//disable eureka client
        //"spring.cloud.loadbalancer.enabled=false", // Disable LoadBalancer
        //"product-service.url=http://localhost:8083" // Override product service URL
        //"spring.redis.host=disabled"

})
public class OrderControllerIntegrationTest {


  @Autowired
  private WebTestClient webClient;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  private String apiUrl;

  private static MockWebServer mockWebServer;

  private static RedisServer redisServer;

  @Container
  public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.0")
          .withDatabaseName("order_service_test")
          .withUsername("testuser")
          .withPassword("testpassword")
          .waitingFor(Wait.forListeningPort());

  @BeforeAll
  static void setUp() throws Exception {
    //start postgres container
    postgresContainer.start();

    //wait for PostgreSQL to be ready
    while (!postgresContainer.isRunning()){
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgresContainer.getUsername());
    System.setProperty("spring.datasource.password", postgresContainer.getPassword());

    //start redis server
    redisServer = new RedisServer(); //bind to a random port
    redisServer.start();
    //wait for redis server to be ready
    while (!redisServer.isActive()){
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    //start mock web server
    mockWebServer = new MockWebServer();
    try {;
      mockWebServer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterAll
  static void tearDown() {
    //stop postgres container
    if (postgresContainer != null && postgresContainer.isRunning()) {
      postgresContainer.stop();
    }

    //stop redis server
    if (redisServer != null) {
      redisServer.stop();
    }

    //stop mock web server
    if (mockWebServer != null) {
      try {
        mockWebServer.shutdown();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }



  @Test
  @DisplayName("Check Get All Orders(GET /api/v1/order)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void testGetAllOrdersSuccess() throws Exception {
    //send a GET request to get all orders
    var result = mockMvc.perform(get(apiUrl + "/order"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

    //assert the response
    var response = result.getResponse().getContentAsString();
    assertNotNull(response);
    //print the response
    System.out.println("Response: " + response);

  }



  @Test
  void testGetOrderDtoById() throws Exception {

  }

  @Nested
  @Import(TestConfig.class)
  class testOrderCreation{

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
      String mockWebServerUrl = mockWebServer.url("/api/v1").toString();
      System.out.println("Injected MockWebServer URL: " + mockWebServerUrl);
      registry.add("product-service.url", () -> mockWebServerUrl);
    }

    @Test
    void testCreateOrder() throws Exception {
      var productId = "123e4567-e89b-12d3-a456-426614174000";
      String productName = "Test Product";
      int productQuantity = 10;
      String productServiceResponse = getString(productId, productName, productQuantity);
      mockWebServer.enqueue(new MockResponse()
              .setBody(productServiceResponse)
              .addHeader("Content-Type", "application/json"));

      //prepare the CreateOrderDto request
      List<OrderItemDto> orderItems = Arrays.asList(
              new OrderItemDto(UUID.fromString(productId), productName, productQuantity)
      );

      var createOrderRequest = new CreateOrderDto(
              "John Doe " + System.currentTimeMillis(),
              "john@email.com",
              "123 Main St, City, Country",
              orderItems
      );

      var jsonOrder = objectMapper.writeValueAsString(createOrderRequest);

      //send a POST request to create an order
      var orderResponse  = webClient.post()
              .uri(apiUrl + "/order/reactive")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(jsonOrder)
              .exchange()

              .expectStatus().isOk()
              .expectHeader().contentType(MediaType.APPLICATION_JSON)
              .expectBody(Map.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(orderResponse);
      Map<String, Object> orderData = (Map<String, Object>) orderResponse.get("data");
      assertNotNull(orderData);

    }

    @NotNull
    private static String getString(String productId, String productName, int productQuantity) {
      var productPrice = new BigDecimal("10.00");
      LocalDate expiryDate = LocalDate.of(2025, 12, 31);
      int availableStock = 50;
      String productCategory = "Electronics";
      String productDescription = "Test Product Description";

      //stub the product service response using MockWebServer
      String productServiceResponse = """
            
            {
                "productId": "%s",
                "productName": "%s",
                "productCategory": "%s",
                "productDescription": "%s",
                "productQuantity": %d,
                "availableStock": %d,
                "expiryDate": "%s",
                "productPrice": %s
            }
            
            """.formatted(
              productId,
              productName,
              productCategory,
              productDescription,
              productQuantity,
              availableStock,
              expiryDate,
              productPrice
      );
      return productServiceResponse;
    }

  }



//NOTE: To complete the test logics in this class, I need to sort out first how to create order

}
