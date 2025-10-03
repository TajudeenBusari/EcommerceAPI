/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.product_service.controller;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.tjtechy.UpdateInventoryDto;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.tjtechy.CreateInventoryDto;
import com.tjtechy.InventoryDto;
import com.tjtechy.Result;
import com.tjtechy.client.InventoryServiceClient;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.testcontainers.shaded.com.google.common.base.Preconditions.checkState;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
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
@EnableAutoConfiguration(exclude ={
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
} )
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Add this
@ActiveProfiles("test")
@Tag("ProductServiceControllerIntegrationTest")

public class ProductServiceControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;


  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Value("${inventory-service.base-url}")
  private String inventoryServiceBaseUrl;

  private static final String CREATE_INVENTORY_SERVICE_URL = "/api/v1/inventory/internal/create";
  private static final String GET_INVENTORY_BY_PRODUCT_URL = "/api/v1/inventory/internal/product";
  private static final String UPDATE_INVENTORY_URL = "/api/v1/inventory/internal/update";
  private static final String DELETE_INVENTORY_URL = "/api/v1/inventory";


  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
          .withDatabaseName("productdb")
          .withUsername("postgres")
          .withPassword("postgres");

//  private static final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
  private static final WireMockServer wireMockServer;

  static {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());
  }

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
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
    // Reset the WireMock server before each test
    wireMockServer.resetAll();
  }


  //helper method to create product with inventory
  /**
   * This helper method will be used for when inventory service is not available.
   * @return
   * @throws Exception
   */
  private Map<String, Object> createProductWithInventory() throws Exception {

    var uniqueProductName = "product-" + System.currentTimeMillis();

    var createProductDto = Map.of(
            "productName", uniqueProductName,
            "productDescription", uniqueProductName + " description",
            "productCategory", uniqueProductName + " category",
            "productQuantity", 10,
            "availableStock", 10,
            "productPrice", 999.99,
            "manufacturedDate", "2025-02-20",
            "expiryDate", "2027-02-20"
    );

    //using restTemplate to call a rest call
    //if you must use this to create a product, you must stub the inventory creation first
    var url = "http://localhost:" + port + baseUrl + "/product/with-inventory/externalized";
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(createProductDto), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> savedProduct = (Map<String, Object>) responseBody.get("data");
    var productId = UUID.fromString((String) savedProduct.get("productId"));
    assertThat(savedProduct.get("productId")).isEqualTo(productId.toString());
    assertThat(savedProduct.get("productName")).isEqualTo(uniqueProductName);
    System.out.println(savedProduct.get("productId"));
    System.out.println(savedProduct.get("productName"));

    return savedProduct;
  }

  //helper method to create product without inventory

  /**
   * Used for just creating a product without inventory.
   * @return
   * @throws Exception
   */
  private Map<String, Object> createProductWithoutInventory() throws Exception {
    var uniqueProductName = "product-" + System.currentTimeMillis();

    var createProductDto = Map.of(
            "productName", uniqueProductName,
            "productDescription", uniqueProductName + " description",
            "productCategory", uniqueProductName + " category",
            "productQuantity", 10,
            "availableStock", 10,
            "productPrice", 999.99,
            "manufacturedDate", "2025-02-20",
            "expiryDate", "2027-02-20"
    );

    //using restTemplate to call a rest call
    var url = "http://localhost:" + port + baseUrl + "/product";
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(createProductDto), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    //parse the response body
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> savedProduct = (Map<String, Object>) responseBody.get("data");
    var productId = UUID.fromString((String) savedProduct.get("productId"));
    assertThat(savedProduct.get("productId")).isEqualTo(productId.toString());

    return savedProduct;

  }

  /**
   * This method will be used to test the product creation with inventory
   * @return
   * @throws Exception
   */
  private Map<String, Object> createProductAndInventoryWithInventoryExternalized() throws Exception {

    //stub the inventory service first
    var tempProductId = UUID.randomUUID();
    var inventoryId = 100L;

    //Not really used, but just to show the structure
    var createInventoryDto = new CreateInventoryDto(
            tempProductId,
            10, //available stock
            1, //reserved quantity
            //expiry date must be in the future
            LocalDate.now().plusDays(30)
    );
    var inventoryDto = new InventoryDto(
            inventoryId,
            tempProductId, // temporary placeholder
            1,
            10);

    var createInventoryResponse = new Result("Inventory created successfully", true, inventoryDto, 200);

    //stub wiremock to accept any productId
    WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(CREATE_INVENTORY_SERVICE_URL))
            .withRequestBody(WireMock.matchingJsonPath("$.availableStock", equalTo("10")))
            .withRequestBody(WireMock.matchingJsonPath("$.reservedQuantity", equalTo("1")))
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(createInventoryResponse))));

    var uniqueProductName = "product-" + System.currentTimeMillis();
    var createProductDto = Map.of(
            "productName", uniqueProductName,
            "productDescription", uniqueProductName + " description",
            "productCategory", uniqueProductName + " category",
            "productQuantity", 10,
            "availableStock", 10,
            "productPrice", 999.99,
            "manufacturedDate", "2025-02-20",
            "expiryDate", "2027-02-20"
    );

    /**
     * Using restTemplate to call a rest call
     *if you must use this to create a product, you must stub the inventory creation first because
     *the product creation will call the inventory service to create inventory
     */
    var url = "http://localhost:" + port + baseUrl + "/product/with-inventory/externalized";
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(createProductDto), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> savedProduct = (Map<String, Object>) responseBody.get("data");
    var productId = UUID.fromString((String) savedProduct.get("productId"));
    assertThat(savedProduct.get("productId")).isEqualTo(productId.toString());
    assertThat(savedProduct.get("productName")).isEqualTo(uniqueProductName);
    System.out.println(savedProduct.get("productId"));
    System.out.println(savedProduct.get("productName"));

    return savedProduct;
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testCreateProductAndInventoryExternalized() throws Exception {
    // This test will create a product and inventory using the helper method
    Map<String, Object> savedProduct = createProductAndInventoryWithInventoryExternalized();
    assertThat(savedProduct).isNotNull();
    assertThat(savedProduct.get("productId")).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    // Verify that the inventory service was called
    await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
      WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(CREATE_INVENTORY_SERVICE_URL))
              .withRequestBody(WireMock.matchingJsonPath("$.availableStock", equalTo("10")))
              .withRequestBody(WireMock.matchingJsonPath("$.reservedQuantity", equalTo("1")))
              .withRequestBody(WireMock.matchingJsonPath("$.productId")) // just checks that productId exists
      );
    });
  }

  /**
   * Test to check if the application context loads successfully.
   * This is a simple test to ensure that the application can start without any issues.
   */
  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void contextLoads() {

    assertThat(restTemplate).isNotNull();
  }


  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test to add product with inventory externalized - inventory service not available")
  public void testAddProductWithInventoryExternalizedInventoryServiceNotAvailable() throws Exception {


    WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(CREATE_INVENTORY_SERVICE_URL))
            .willReturn(WireMock.aResponse()
                    .withStatus(503)
                    .withBody("Service Unavailable")));

    Logger logger = (Logger) LoggerFactory.getLogger(InventoryServiceClient.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);


    // Attempt to create a product with inventory
    var savedProduct = createProductWithInventory();

    var expectedProductId = UUID.fromString((String) savedProduct.get("productId"));
    var productName = (String) savedProduct.get("productName");

    // Assert that the product was created successfully
    assertThat(expectedProductId).isNotNull();
    assertThat(productName).isNotNull();

    // Verify that the inventory service was not called and an error was logged
    //*******Error occurred while creating inventory for product b5e3d3a5-7f33-467d-a4a2-f89c1e1acf47
    // Wait for log to appear
    boolean[] errorLogged = {false};
    await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
              errorLogged[0] = listAppender.list.stream()
                      .anyMatch(event ->
                              event.getFormattedMessage().contains("Error occurred while creating inventory for product") &&
                                      event.getFormattedMessage().contains("503"));
              assertThat(errorLogged[0]).isTrue();
            });
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test to update product with inventory externalized- successful update")
  public void testUpdateProductWithInventoryExternalized() throws Exception {

    //create a product with inventory first
    var savedProduct = createProductAndInventoryWithInventoryExternalized();
    var productId = UUID.fromString((String) savedProduct.get("productId"));
    var updatedProductName = "Updated Product Name";
    var updateProductDto = Map.of(
            "productName", updatedProductName,
            "productDescription", "Updated description",
            "productCategory", "Updated category",
            "productQuantity", 20,
            "availableStock", 20,
            "productPrice", 199.99
    );

    /**
     * stubbing the get inventory by product id endpoint will also require us to
     * create
     */

    var tempProductId = productId;
    var inventoryId = 100L;
    var inventoryDto = new InventoryDto(
            inventoryId,
            tempProductId, // temporary placeholder
            1,
            10); //available stock

    var getInventoryResponse = new Result("Inventory with productId: " + inventoryDto.productId() + " retrieved successfully", true, inventoryDto, 200);
    WireMock.stubFor(WireMock.get(GET_INVENTORY_BY_PRODUCT_URL + "/" + tempProductId)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(getInventoryResponse))));

    //stub the inventory update endpoint
    var updateInventoryDto = new UpdateInventoryDto(
            productId,
            20, //available stock
            1 //reserved quantity
    );

    var updateInventoryResponse = new Result("Inventory updated successfully", true, inventoryDto, 200);
    WireMock.stubFor(WireMock.put(WireMock.urlEqualTo(UPDATE_INVENTORY_URL +"/"+ inventoryId))
                    .withRequestBody(WireMock.matchingJsonPath("$.productId", equalTo(tempProductId.toString())))
            .withRequestBody(WireMock.matchingJsonPath("$.availableStock", equalTo("20")))
            .withRequestBody(WireMock.matchingJsonPath("$.reservedQuantity", equalTo("1")))
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(updateInventoryResponse))));

    //now trigger the product update
    var url = "http://localhost:" + port + baseUrl + "/product/" + productId + "/with-inventory/externalized";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(updateProductDto), headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);

   await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
      WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(GET_INVENTORY_BY_PRODUCT_URL + "/" + tempProductId)));
      WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo(UPDATE_INVENTORY_URL +"/"+ inventoryId))
              .withRequestBody(WireMock.matchingJsonPath("$.availableStock", equalTo("20")))
              .withRequestBody(WireMock.matchingJsonPath("$.reservedQuantity", equalTo("1")))
              .withRequestBody(WireMock.matchingJsonPath("$.productId", equalTo(tempProductId.toString()))));
    });

  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test add product")
  public void testAddProductSuccess() throws Exception {
    // This test will create a product without inventory
    Map<String, Object> savedProduct = createProductWithoutInventory();
    assertThat(savedProduct).isNotNull();
    assertThat(savedProduct.get("productId")).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    // Verify that the product was created successfully
    var productId = UUID.fromString((String) savedProduct.get("productId"));
    assertThat(productId).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    System.out.println(savedProduct.get("productId"));
    System.out.println(savedProduct.get("productName"));
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test delete product by id success")
  public void testDeleteProductById() throws Exception {
    // Create a product first
    Map<String, Object> savedProduct = createProductWithoutInventory();
    assertThat(savedProduct).isNotNull();
    assertThat(savedProduct.get("productId")).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    var productId = UUID.fromString((String) savedProduct.get("productId"));

    // Now delete the product
    var url = "http://localhost:" + port + baseUrl + "/product/" + productId;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify that the product was deleted successfully
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);
    assertThat(responseBody.get("message")).isEqualTo("Delete One Success");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test bulk delete products by ids success")
  public void testBulkDeleteProductsByIds() throws Exception {
    // Create multiple products first
    Map<String, Object> product1 = createProductWithoutInventory();
    Map<String, Object> product2 = createProductWithoutInventory();
    assertThat(product1).isNotNull();
    assertThat(product2).isNotNull();

    var productId1 = UUID.fromString((String) product1.get("productId"));
    var productId2 = UUID.fromString((String) product2.get("productId"));

    // Now bulk delete the products
    var url = "http://localhost:" + port + baseUrl + "/product/bulk-delete";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(List.of(productId1.toString(), productId2.toString())), headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify that the products were deleted successfully
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);
    assertThat(responseBody.get("message")).isEqualTo("Bulk Delete Success");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test delete product by id with inventory externalized success")
  public void testDeleteProductByIdWithInventoryExternalized() throws Exception {
    // Create a product with inventory first
    Map<String, Object> savedProduct = createProductAndInventoryWithInventoryExternalized();
    assertThat(savedProduct).isNotNull();
    assertThat(savedProduct.get("productId")).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    var productId = UUID.fromString((String) savedProduct.get("productId"));
    var inventoryId = 100L; // Assuming the inventory ID is 1 for this test

    var getInventoryByProductIdResponse = new Result("Inventory with productId: " + productId + " retrieved successfully", true, new InventoryDto(inventoryId, productId,1, 10), 200);
    // Stub the inventory service to return the inventory for the product
    WireMock.stubFor(WireMock.get(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(getInventoryByProductIdResponse))));

    var deleteInventoryResponse = new Result("Inventory deleted successfully", true, null, 200);
    // Stub the inventory service to delete the inventory for the product
    WireMock.stubFor(WireMock.delete(DELETE_INVENTORY_URL + "/" + inventoryId)

            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(deleteInventoryResponse))));

    // Now delete the product with inventory
    var url = "http://localhost:" + port + baseUrl + "/product/delete/with-inventory/externalized/" + productId;
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
    if (response.getStatusCode() != HttpStatus.OK) {
      System.out.println("Error response: " + response.getBody());
    }
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


    // Verify that the product was deleted successfully
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);
    assertThat(responseBody.get("message")).isEqualTo("Delete One Success Using Externalized");

    // Verify WireMock interactions
    await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
      WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId)));
      WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlEqualTo(DELETE_INVENTORY_URL + "/" + inventoryId)));
    });
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test bulk delete products with inventories externalized success")
  public void testBulkDeleteProductsWithInventoriesExternalized() throws Exception {
    // Create multiple products with inventories first
    Map<String, Object> product1 = createProductAndInventoryWithInventoryExternalized();
    Map<String, Object> product2 = createProductAndInventoryWithInventoryExternalized();
    assertThat(product1).isNotNull();
    assertThat(product2).isNotNull();

    var productId1 = UUID.fromString((String) product1.get("productId"));
    var productId2 = UUID.fromString((String) product2.get("productId"));

    var inventoryId1 = 100L; // Assuming the inventory ID is 1 for this test
    var inventoryId2 = 101L; // Assuming the inventory ID is 2 for this test

    var getInventoryByProductIdResponse1 = new Result("Inventory with productId: " + productId1 + " retrieved successfully", true, new InventoryDto(inventoryId1, productId1, 1,  10), 200);
    var getInventoryByProductIdResponse2 = new Result("Inventory with productId: " + productId2 + " retrieved successfully", true, new InventoryDto(inventoryId2, productId2, 1,10), 200);

    // Stub the inventory service to return the inventories for the products
    WireMock.stubFor(WireMock.get(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId1)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(getInventoryByProductIdResponse1))));
    WireMock.stubFor(WireMock.get(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId2)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(getInventoryByProductIdResponse2))));

    var deleteInventoryResponse = new Result("Inventories deleted successfully", true, null, 200);
    // Stub the inventory service to delete the inventories for the products
    WireMock.stubFor(WireMock.delete(DELETE_INVENTORY_URL + "/" + inventoryId1)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(deleteInventoryResponse))));
    WireMock.stubFor(WireMock.delete(DELETE_INVENTORY_URL + "/" + inventoryId2)
            .willReturn(WireMock.okJson(objectMapper.writeValueAsString(deleteInventoryResponse))));

    // Now bulk delete the products with inventories
    var url = "http://localhost:" + port + baseUrl + "/product/bulk-delete/with-inventory/externalized";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(List.of(productId1.toString(), productId2.toString())), headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    // Verify that the products were deleted successfully
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);
    assertThat(responseBody.get("message")).isEqualTo("Bulk Delete With Inventories Using Externalized Success");

    // Verify WireMock interactions
    await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
      WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId1)));
      WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo(GET_INVENTORY_BY_PRODUCT_URL + "/" + productId2)));
      WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlEqualTo(DELETE_INVENTORY_URL + "/" + inventoryId1)));
      WireMock.verify(WireMock.deleteRequestedFor(WireMock.urlEqualTo(DELETE_INVENTORY_URL + "/" + inventoryId2)));
    });
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test bulk delete products")
  public void testBulkDeleteProducts() throws Exception {
    // Create multiple products first
    Map<String, Object> product1 = createProductWithoutInventory();
    Map<String, Object> product2 = createProductWithoutInventory();
    assertThat(product1).isNotNull();
    assertThat(product2).isNotNull();

    var productId1 = UUID.fromString((String) product1.get("productId"));
    var productId2 = UUID.fromString((String) product2.get("productId"));

    // Now bulk delete the products
    var url = "http://localhost:" + port + baseUrl + "/product/bulk-delete";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(List.of(productId1.toString(), productId2.toString())), headers);
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify that the products were deleted successfully
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("flag")).isEqualTo(true);
    assertThat(responseBody.get("message")).isEqualTo("Bulk Delete Success");
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @DisplayName("Test get all products success")
  public void testGetAllProductsSuccess() throws Exception {
    // Create a product first
    Map<String, Object> savedProduct = createProductWithoutInventory();
    assertThat(savedProduct).isNotNull();
    assertThat(savedProduct.get("productId")).isNotNull();
    assertThat(savedProduct.get("productName")).isNotNull();

    // Now get all products
    var url = "http://localhost:" + port + baseUrl + "/product";
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Verify that the response contains the created product
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    List<Map<String, Object>> products = (List<Map<String, Object>>) responseBody.get("data");
    assertThat(products).isNotNull();
    assertThat(products.stream().anyMatch(product -> product.get("productId").equals(savedProduct.get("productId")))).isTrue();
  }

}
