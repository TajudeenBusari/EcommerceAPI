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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.test.web.reactive.server.WebTestClient;

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
        "product-service.url=http://localhost:8083" // Override product service URL
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

  private static RedisServer redisServer;
  private static WireMockServer wireMockServer;
  private static final int WIREMOCK_PORT = 8083;
  private static final String PRODUCT_SERVICE_URL = "http://localhost:" + WIREMOCK_PORT + "/api/v1/product";

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

    //start wiremock server to mock product service
    wireMockServer = new WireMockServer(WIREMOCK_PORT);
    wireMockServer.start();
    WireMock.configureFor("localhost", WIREMOCK_PORT);
    //stub the product service
    stubProductService();

  }
  private static void stubProductService() throws Exception {
    //stub for product service
    String productId = "123e4567-e89b-12d3-a456-426614174000";
    String uniqueProductName = "Product" + System.currentTimeMillis();
//    var createProductRequest =  Map.of(
//            "productName", uniqueProductName,
//            "productDescription", "This is a test product",
//            "productCategory", "Electronics",
//            "productQuantity", 100,
//            "availableStock", 50,
//            "productPrice", BigDecimal.valueOf(1000),
//            "manufacturedDate", LocalDate.now(),
//            "expiryDate", LocalDate.now().plusDays(30)
//    );
//    var json = new ObjectMapper().writeValueAsString(createProductRequest);
//    wireMockServer.stubFor(get(urlEqualTo("/api/v1/product/" +productId))
    wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/product/" +productId))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                            """
                                 
                                        "productId": "%s",
                                        "productName": "%s",
                                        "productDescription": "This is a test product",
                                        "productCategory": "Electronics",
                                        "productQuantity": 100,
                                        "availableStock": 50,
                                        "productPrice": 1000.00,
                                        "manufacturedDate": "%s",
                                        "expiryDate": "%s"{
                                    }
                                    """.formatted(productId, uniqueProductName, LocalDate.now(), LocalDate.now().plusDays(30) )
                    )));


  }

  @AfterAll
  static void tearDown() {
    if (postgresContainer != null && postgresContainer.isRunning()) {
      postgresContainer.stop();
    }

    if (wireMockServer != null) {
      wireMockServer.stop();
    }
    if (redisServer != null) {
      redisServer.stop();
    }
  }


  @Test
  void testCreateOrder() throws Exception {
    var productId = "123e4567-e89b-12d3-a456-426614174000";
    String productName = "Test Product";
    int productQuantity = 100;


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
  void testGetOrderById() throws Exception {
    var orderId = 1000L;
    //stub the product service
    String productId = "123e4567-e89b-12d3-a456-426614174000";
    String uniqueProductName = "Product" + System.currentTimeMillis();
    var createProductRequest =  Map.of(
            "productName", uniqueProductName,
            "productDescription", "This is a test product",
            "productCategory", "Electronics",
            "productQuantity", 100,
            "availableStock", 50,
            "productPrice", BigDecimal.valueOf(1000),
            "manufacturedDate", LocalDate.now(),
            "expiryDate", LocalDate.now().plusDays(30)
    );
    var json = objectMapper.writeValueAsString(createProductRequest);
    wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/product/" +productId))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(json)));

    //send a GET request to get an order by ID
    var result = mockMvc.perform(get(apiUrl + "/orderDto/" + orderId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

  }
//  //helper method to create a product
//  private Map<String, Object> createProduct(String apiUrl) throws Exception {
//
//    WebTestClient wireMockClient = WebTestClient
//            .bindToServer()
//            .baseUrl(PRODUCT_SERVICE_URL)
//            .build();
//
//    String uniqueProductName = "Product" + System.currentTimeMillis();
//    var createProductRequest = Map.of(
//            "productName", uniqueProductName,
//            "productDescription", "This is a test product",
//            "productCategory", "Electronics",
//            "productQuantity", 100,
//            "availableStock", 50,
//            "productPrice", 1000.00,
//            "manufacturedDate", LocalDate.now(),
//            "expiryDate", LocalDate.now().plusDays(30)
//    );
//
//    var json = objectMapper.writeValueAsString(createProductRequest);
//    var result = wireMockClient.post()
//            .uri(apiUrl + "/product")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(json)
//            .exchange()
//            .expectStatus().isOk()
//            .expectBody(Map.class)
//            .returnResult()
//            .getResponseBody();
//
//    assertNotNull(result);
//    Map<String, Object> productData = (Map<String, Object>) result.get("data");
//    assertNotNull(productData);
//
//
//
//    //validate that the product ID is present in the response
//    var productId = (String) productData.get("productId");
//    var productName = (String) productData.get("productName");
//    var productQuantity = (Integer) productData.get("productQuantity");
//    if (productId == null || productId.isEmpty()) {
//      throw new RuntimeException("Product ID is missing in the response");
//    }
//    System.out.println("Product ID: " + productId);
//    return productData;
//
//  }

}
