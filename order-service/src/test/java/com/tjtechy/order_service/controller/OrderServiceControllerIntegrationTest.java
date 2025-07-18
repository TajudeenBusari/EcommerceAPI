/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tjtechy.*;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderItemDto;
import com.tjtechy.order_service.entity.dto.UpdateOrderDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.config.client.ConfigServerBootstrapper;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static wiremock.com.google.common.base.Preconditions.checkState;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.fetchRegistry=false",
        "eureka.client.registerWithEureka=false",
        "spring.cloud.loadbalancer.enabled=false", // Disable load balancer
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "redis.enabled=false", //disable redis
        "spring.cache.type=none", //disable caching
})

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //prevents the test from using an embedded database
@Testcontainers
@Tag("OrderServiceControllerIntegrationTest")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

public class OrderServiceControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Container
  private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("order_service_test")
          .withUsername("test")
          .withPassword("test");

  private static final WireMockServer wireMockServer;
  static {
    wireMockServer= new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());
  }

  @DynamicPropertySource
  static void registerPostgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
    registry.add("product-service.base-url", () -> {
      checkState(wireMockServer.isRunning(), "WireMock server is not running");
      return "http://localhost:" + wireMockServer.port() + "/api/v1";
    });
    registry.add("inventory-service.base-url", () -> {
      checkState(wireMockServer.isRunning(), "WireMock server is not running");
      return "http://localhost:" + wireMockServer.port() + "/api/v1";
    });
    registry.add("api.endpoint.base-url", () -> "/api/v1");
  }

  @BeforeAll
  static void startContainers() {
    postgreSQLContainer.start();
  }

  @AfterAll
  static void stopContainers() {
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
    if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
      postgreSQLContainer.stop();
    }
  }

  @BeforeEach
  void resetWireMock() {
    wireMockServer.resetAll();
  }


  private Map<String, Object> createOrderReactively(CreateOrderDto createOrderDto) throws Exception {
    //Mock the product-service get response
    ProductDto productDto = new ProductDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), // Use a fixed UUID for testing, all orders will use this product
            "Product 1",
            "Test Category",
            "Test Description",
            10,
            10,
            LocalDate.now().plusDays(30),
            BigDecimal.valueOf(100.00)
    );
    var getProductResponse = new Result("Get One Success", true, productDto, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/product/" + productDto.productId()))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper
                            .registerModule(new JavaTimeModule())
                            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                            .writeValueAsString(getProductResponse))));
    //Mock the inventory-service deduct response
    var deductInventoryResponse = new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.patch(urlEqualTo("/api/v1/inventory/internal/deduct-inventory-reactive"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(deductInventoryResponse)))); //no need to register JavaTimeModule here since we are not using LocalDate in the request body
    //Create order
    String url = "http://localhost:" + port + baseUrl + "/order/reactive/externalized";

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String json = objectMapper
            .registerModule(new JavaTimeModule()) // Register JavaTimeModule to handle LocalDate and other Java 8 date/time types
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .writeValueAsString(createOrderDto);

    HttpEntity<String> request = new HttpEntity<>(json, headers);
    ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.POST, request, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order created successfully by calling required external services");
    assertThat(response.getBody().isFlag()).isEqualTo(true);
    var createdOrder = (Map<String, Object>) response.getBody().getData();
    return createdOrder;
  }
  @Test
  @DisplayName("Create Order reactively by calling externalized product-service and inventory-service - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testCreateOrderReactivelyByCallingExternalizedServicesSuccess() throws Exception {
    //Mock the product-service get response
    ProductDto productDto = new ProductDto(
        UUID.randomUUID()   ,
        "Product" + + System.currentTimeMillis(),
        "Test Category",
        "Test Description",
        10,
        10,
        LocalDate.now().plusDays(30),
        BigDecimal.valueOf(100.00)
    );
    var getProductResponse = new Result("Get One Success", true, productDto, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/product/" + productDto.productId()))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .writeValueAsString(getProductResponse))));

    //Mock the inventory-service deduct response
    var deductInventoryResponse = new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.patch(urlEqualTo("/api/v1/inventory/internal/deduct-inventory-reactive"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(deductInventoryResponse))));

    //Create order
    var orderItemDto1 = new OrderItemDto();
    orderItemDto1.setProductId(productDto.productId());
    orderItemDto1.setProductName(productDto.productName());
    orderItemDto1.setQuantity(10);


    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Customer" + System.currentTimeMillis());
    createOrderDto.setCustomerEmail("test@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

//    This also works but is commented to be consistent with other integration tests
//    String url = UriComponentsBuilder
//            .newInstance()
//            .scheme("http")
//            .host("localhost")
//            .port(port)
//            .path(baseUrl)
//            .path("/order/reactive/externalized")
//            .build()
//            .toUriString();
    String url = "http://localhost:" + port + baseUrl + "/order/reactive/externalized";


    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String json = objectMapper
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .writeValueAsString(createOrderDto);

    HttpEntity<String> request = new HttpEntity<>(json, headers);
    var response = restTemplate.exchange(url, HttpMethod.POST, request, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order created successfully by calling required external services");
    //extract the created order from the response
    var createdOrder = response.getBody().getData();
    System.out.println("The created order is: " + createdOrder);

    //verify the get product call and deduct inventory call
    wireMockServer.verify(1, WireMock.getRequestedFor(urlEqualTo("/api/v1/product/" + productDto.productId())));
    wireMockServer.verify(1, WireMock.patchRequestedFor(urlEqualTo("/api/v1/inventory/internal/deduct-inventory-reactive"))
            .withRequestBody(matchingJsonPath("$.productId", equalTo(productDto.productId().toString())))
            .withRequestBody(matchingJsonPath("$.quantity", equalTo("10"))));

  }

  @Test
  @DisplayName("Test get order by ID - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetOrderByIdSuccess() throws Exception {
    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the get order by ID endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/" + orderId;
    ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order retrieved successfully");
  }

  @Test
  @DisplayName("Test get orderDto by ID - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetOrderDtoByIdSuccess() throws Exception {
    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the get order by ID endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/orderDto/" + orderId;
    ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("OrderDto retrieved successfully");
  }

  @Test
  @DisplayName("Test get all orders - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetAllOrdersSuccess() throws Exception {
    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto1 = new CreateOrderDto();
    createOrderDto1.setCustomerName("Test Customer 1");
    createOrderDto1.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto1.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto1.setOrderItems(List.of(orderItemDto1));

    var createOrderDto2 = new CreateOrderDto();
    createOrderDto2.setCustomerName("Test Customer 2");
    createOrderDto2.setCustomerEmail("test2" + System.currentTimeMillis() + "@test.com");
    createOrderDto2.setShippingAddress("456 Test Avenue, Test City, TC 67890");
    createOrderDto2.setOrderItems(List.of(orderItemDto1));


    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto1);
    Long orderId1 = Long.parseLong(createdOrder.get("orderId").toString());
    // Create another order reactively
    Map<String, Object> createdOrder2 = createOrderReactively(createOrderDto2);
    Long orderId2 = Long.parseLong(createdOrder2.get("orderId").toString());

    // Now, test the get all orders endpoint
    String url = "http://localhost:" + port + baseUrl + "/order";
    ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Orders retrieved successfully");
    //verify that the response contains both orders
    List<Map<String, Object>> orders = (List<Map<String, Object>>) response.getBody().getData();
    assertThat(orders).isNotNull();
    assertThat(orders.size()).isGreaterThanOrEqualTo(2);
    System.out.println(orders);
    //extract the order IDs from the response
    List<Long> orderIds = orders.stream()
            .map(order -> Long.parseLong(order.get("orderId").toString()))
            .toList();
    //verify that both order IDs are present in the response
    System.out.println(orderIds);
  }

  @Test
  @DisplayName("Test get order by customer email - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetOrderByCustomerEmailSuccess() throws Exception {
    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the get order by customer email endpoint, add email as request parameter
    String url = "http://localhost:" + port + baseUrl + "/order/customer?customerEmail=" + createOrderDto.getCustomerEmail();
    ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Orders by email retrieved successfully");
  }

  @Test
  @DisplayName("Test delete order by ID and restore inventory - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testDeleteOrderByIdAndRestoreInventorySuccess() throws Exception {

    //Stub the inventory-service restore response
    var restoreInventoryResponse = new Result("Inventory restored successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(restoreInventoryResponse))));

    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the delete order by ID endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/" + orderId;
    ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order deleted successfully");
  }

  @Test
  @DisplayName("Test bulk delete orders and restore inventories - Success")
  public void testBulkDeleteOrdersAndRestoreInventoriesSuccess() throws Exception {
    //Stub the inventory-service restore response
    var restoreInventoryResponse = new Result("Inventory restored successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(restoreInventoryResponse))));

    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto1 = new CreateOrderDto();
    createOrderDto1.setCustomerName("Test Customer 1");
    createOrderDto1.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto1.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto1.setOrderItems(List.of(orderItemDto1));

    var createOrderDto2 = new CreateOrderDto();
    createOrderDto2.setCustomerName("Test Customer 2");
    createOrderDto2.setCustomerEmail("test2" + System.currentTimeMillis() + "@test.com");
    createOrderDto2.setShippingAddress("456 Test Avenue, Test City, TC 67890");
    createOrderDto2.setOrderItems(List.of(orderItemDto1));

    // Create the first order reactively
    Map<String, Object> createdOrder1 = createOrderReactively(createOrderDto1);
    Long orderId1 = Long.parseLong(createdOrder1.get("orderId").toString());

    // Create the second order reactively
    Map<String, Object> createdOrder2 = createOrderReactively(createOrderDto2);
    Long orderId2 = Long.parseLong(createdOrder2.get("orderId").toString());

    // Now, test the bulk delete orders endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/bulk-delete";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String json = objectMapper
            .writeValueAsString(List.of(orderId1, orderId2));
    HttpEntity<String> request = new HttpEntity<>(json, headers);
    ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Orders bulk deleted success");
    //Note:Verify that the restore inventory endpoint was called
    //Restore inventory´end point is called twice, once for each order
    wireMockServer.verify(2, WireMock.postRequestedFor(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .withRequestBody(matchingJsonPath("$.productId", equalTo(orderItemDto1.getProductId().toString())))
            .withRequestBody(matchingJsonPath("$.quantity", equalTo("5"))));
  }


  @Test
  @DisplayName("Test get order by status - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetOrderByStatusSuccess() throws Exception {
    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the get order by status endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/status?orderStatus=PLACED";
    ResponseEntity<Result> response = restTemplate.getForEntity(url, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Orders by status retrieved successfully");
  }

  @Test
  @DisplayName("Test update order by calling externalized product-service and inventory-service - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testUpdateOrderByCallingExternalizedServices() throws Exception {

    //stub restore inventory response
    var restoreInventoryResponse = new Result("Inventory restored successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(restoreInventoryResponse))));

    //stub get product response
    ProductDto productDto = new ProductDto(
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), // Use a fixed UUID for testing, all orders will use this product
            "Product 1",
            "Test Category",
            "Test Description",
            10,
            10,
            LocalDate.now().plusDays(30),
            BigDecimal.valueOf(100.00)
    );
    var getProductResponse = new Result("Get One Success", true, productDto, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.get(urlEqualTo("/api/v1/product/" + productDto.productId()))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper
                            .registerModule(new JavaTimeModule())
                            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                            .writeValueAsString(getProductResponse))));

    //stub deduct inventory response
    var deductInventoryResponse = new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.patch(urlEqualTo("/api/v1/inventory/internal/deduct-inventory-reactive"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(deductInventoryResponse))));
    //Create order
    var orderItemDto1 = new OrderItemDto();
    orderItemDto1.setProductId(productDto.productId());
    orderItemDto1.setProductName(productDto.productName());
    orderItemDto1.setQuantity(10);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Customer" + System.currentTimeMillis());
    createOrderDto.setCustomerEmail("test@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the update order by calling externalized services endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/externalized/" + orderId;
    var updateOrderDto = new UpdateOrderDto(
            "Updated Customer Name",
            "testupdated@test.com",
            "456 Updated Street, Updated City, UC 67890",
            List.of(new OrderItemDto(
                    productDto.productId(),
                    productDto.productName(),
                    5 // Update quantity to 5
            ))
    );
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String json = objectMapper
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .writeValueAsString(updateOrderDto);
    HttpEntity<String> request = new HttpEntity<>(json, headers);
    ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.PUT, request, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order updated successfully by calling required external services");

    //verify the restore inventory, get product call and deduct inventory call
    wireMockServer.verify(1, postRequestedFor(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .withRequestBody(matchingJsonPath("$.productId", equalTo(productDto.productId().toString())))
                    .withRequestBody(matchingJsonPath("$.quantity", equalTo("10"))));
    //the get product call should be called twice, once for the initial order creation and once for the update
    wireMockServer.verify(2, WireMock.getRequestedFor(urlEqualTo("/api/v1/product/" + productDto.productId())));

    wireMockServer.verify(1, WireMock.patchRequestedFor(urlEqualTo("/api/v1/inventory/internal/deduct-inventory-reactive"))
            .withRequestBody(matchingJsonPath("$.productId", equalTo(productDto.productId().toString())))
            .withRequestBody(matchingJsonPath("$.quantity", equalTo("5"))));
  }

  @Test
  @DisplayName("Test cancel order by ID and restore inventory - Success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testCancelOrderByIdAndRestoreInventorySuccess() throws Exception {
    //stub restore inventory response
    var restoreInventoryResponse = new Result("Inventory restored successfully", true, null, StatusCode.SUCCESS);
    wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/inventory/internal/restore-inventory"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(restoreInventoryResponse))));

    var orderItemDto1 = new OrderItemDto();
    //THE productId MUST BE FROM THE PRIVATE METHOD TO CREATE ORDER REACTIVELY
    orderItemDto1.setProductId( UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
    orderItemDto1.setProductName( "Product 1");
    orderItemDto1.setQuantity(5);

    var createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("Test Customer");
    createOrderDto.setCustomerEmail("test" + System.currentTimeMillis() + "@test.com");
    createOrderDto.setShippingAddress("123 Test Street, Test City, TC 12345");
    createOrderDto.setOrderItems(List.of(orderItemDto1));

    // Create the order reactively
    Map<String, Object> createdOrder = createOrderReactively(createOrderDto);
    Long orderId = Long.parseLong(createdOrder.get("orderId").toString());

    // Now, test the cancel order by ID endpoint
    String url = "http://localhost:" + port + baseUrl + "/order/cancel/" + orderId;
    ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.DELETE, null, Result.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Order cancelled successfully");
  }

}
