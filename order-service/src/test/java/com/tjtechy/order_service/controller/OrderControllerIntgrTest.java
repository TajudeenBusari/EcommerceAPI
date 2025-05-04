package com.tjtechy.order_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.ProductDto;
import com.tjtechy.Result;
import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderItemDto;
import com.tjtechy.order_service.repository.OrderRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
//        "spring.datasource.url=jdbc:tc:postgresql:15.2:///testdb",
//        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
//        "spring.datasource.username=test",
//        "spring.datasource.password=test",
//        "spring.redis.host=localhost",
//        "spring.redis.port=6379"
        "api.endpoint.base-url=/api/v1",
        "spring.cloud.config.enabled=false", // Disable Spring Cloud Config
        "eureka.client.enabled=false", // Disable Eureka Client
})
public class OrderControllerIntgrTest {


  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  private static MockWebServer mockWebServer;

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

//  @Autowired
//  private static OrderRepository orderRepository;

  @BeforeAll
  static void setUp() throws Exception {
    //start mock server
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDown() throws IOException {
    //stop mock server
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
    //stop postgres container
    if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
      postgreSQLContainer.stop();
    }

  }

//  @BeforeEach
//  void beforeEach() {
//    //clear the database before each test
//
//    orderRepository.deleteAll();
//  }

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
//    registry.add("spring.redis.host", () -> "localhost");
//    registry.add("spring.redis.port", () -> 6379);
    registry.add("product-service.url", () -> "/api/v1");
  }

  @Nested
  @Import(TestConfig.class)
  class TestOrderCreation {

    @Test
    void testCreateOrder() throws Exception {
      //prepare the product-service mock response
      UUID productId = UUID.fromString("a0eebc3b-1f2d-4b5e-8f3c-7a2b6d5f9c1e");
      ProductDto productDto = new ProductDto(
              productId,
              "productName",
              "ProductCategory",
              "productDescription",
              10,
              1,
              LocalDate.of(2026, 10, 10),
              new BigDecimal("100.0")
      );
      var ProductServiceResponse = new Result("Get One Success", true, productDto, 200);
      String productServiceResponseJson = objectMapper.writeValueAsString(ProductServiceResponse);

      // Enqueue the mock response
      mockWebServer.enqueue(new MockResponse()
              .setResponseCode(200)
              .setBody(productServiceResponseJson)
      );
      //create order item dto
      var orderItemDto = new OrderItemDto();
      orderItemDto.setProductId(productId);
      orderItemDto.setProductName("productName");
      orderItemDto.setQuantity(2);

      // Create the order request
      var orderRequest = new CreateOrderDto();
      orderRequest.setCustomerName("John Doe");
      orderRequest.setCustomerEmail("john@doe.com");
      orderRequest.setShippingAddress("123 Main St, City, Country");
      orderRequest.setOrderItems(List.of(orderItemDto));

      //send a POST request to create an order
      var orderResponse = webTestClient.post()
              .uri("/api/v1/order/reactive")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(orderRequest)
              .exchange()
              .expectStatus().isOk()
              .expectHeader().contentType(MediaType.APPLICATION_JSON)
              .returnResult(Result.class)
              .getResponseBody()
              .blockFirst();

    }
  }

}
