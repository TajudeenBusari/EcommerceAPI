/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import com.tjtechy.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:tc:postgresql:15.0:///inventorydb",
        "eureka.client.fetchRegistry=false",
        "eureka.client.registerWithEureka=false",
        "spring.cloud.loadbalancer.enabled=false", // Disable load balancer
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "redis.enabled=false", //disable redis
        "spring.cache.type=none", //disable caching
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //prevents the test from using an embedded database
@AutoConfigureMockMvc //enables MockMvc for testing the controller
@Tag("InventoryControllerIntegrationTest")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude ={
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
} )
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Add this

@AutoConfigureWebTestClient
//since the application context already loads the webClientBuilder bean, we need to disable the autoconfiguration
//@Import(TestConfig.class)
public class InventoryControllerIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @LocalServerPort
  private int port;

  /**
   * ObjectMapper instance to handle JSON serialization and deserialization.
   * By default, Jackson does not handle Java 8+ time classes(LocalDate, LocalDateTime, Instant, etc.),
   * JavaTimeModule comes fromjackson-datatype-jsr310 dependency and adds support for these types,
   * so the .registerModule(new JavaTimeModule()) line registers this module with the ObjectMapper instance.
   * The .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) ensures that
   * dates are serialized in a readable format(<"expiryDate": "2025-09-03">) instead of as
   * numeric timestamps (<"expiryDate":"1725408000000">).
   * .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS) prevents errors(InvalidDefinitionException)
   * during serialization of an object with no properties. Disabling ensures Jackson output to be {}
   *
   */
  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  private final Faker faker = new Faker(); //initialize Faker to generate random data

  @Container
  public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
          .withDatabaseName("inventory_db")
          .withUsername("postgres")
          .withPassword("password");

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
    registry.add("api.endpoint.base-url", () -> "/api/v1");
  }

  @BeforeAll
  static void setUp() {
    postgreSQLContainer.start();
  }
  @AfterAll
  static void tearDown() {

    if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
      postgreSQLContainer.stop();
    }
  }

  //Helper class to createInventoryDto List data
  private List<CreateInventoryDto> createInventoryDtoList (int count){
    List<CreateInventoryDto> inventoryDtoList = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      CreateInventoryDto createInventoryDto = new CreateInventoryDto(
          UUID.randomUUID(), // Randomly generate a productId
          faker.number().numberBetween(1, 100), // Randomly generate a reserve quantity between 1 and 100
          faker.number().numberBetween(1, 100),// Randomly generate available stock
              LocalDate.now(), // Set manufactured date to today and before expiry date
              LocalDate.now().plusDays(30) // Set expiry date to 30 days in the future


      );
      inventoryDtoList.add(createInventoryDto);
    }
    //validate generated data
    inventoryDtoList.forEach(dto ->{
      assertThat(dto.productId()).isNotNull();
      assertThat(dto.availableStock()).isGreaterThan(0);
      assertThat(dto.reservedQuantity()).isGreaterThan(0);
    });
    return inventoryDtoList;
  }

  private Map<String, Object> createInventory (CreateInventoryDto createInventoryDto) throws Exception {
     var url = "http://localhost:" + port + baseUrl + "/inventory/internal/create";

    var response = webTestClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createInventoryDto)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(response, "Response should not be null");
    assertInstanceOf(Map.class, response.getData());

    @SuppressWarnings("unchecked")
    Map<String, Object> savedInventory = (Map<String, Object>) response.getData();
    return savedInventory;
  }

  @Test
  @DisplayName("Test to create inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testCreateInventorySuccess() throws Exception {
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100),// Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today and before expiry date
        LocalDate.now().plusDays(30) // Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);
    assertThat(savedInventory).isNotNull();
    assertThat(savedInventory.get("inventoryId")).isNotNull();

    System.out.println(savedInventory); //The return data is the inventoryDto (inventoryId, productId, reservedQuantity)
    assertThat(UUID.fromString((String) savedInventory.get("productId"))).isEqualTo(createInventoryDto.productId());
    System.out.println(savedInventory.get("reservedQuantity"));
    System.out.println(savedInventory.get("inventoryId"));
    System.out.println(savedInventory.get("availableStock"));
  }

  @Test
  @DisplayName("Test to get inventory by id success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetInventoryByIdSuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100), // Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today and before expiry date
        LocalDate.now().plusDays(365)// Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the inventoryId from the saved inventory
    Long expectedInventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Now get the inventory by id
    var url = "http://localhost:" + port + baseUrl + "/inventory/" + expectedInventoryId;

    var response = webTestClient.get().uri(url)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();

    assert response != null;
    assertInstanceOf(Map.class, response.getData());
    @SuppressWarnings("unchecked")
    Map<String, Object> inventoryDto = (Map<String, Object>) response.getData();
    assertNotNull(inventoryDto, "Retrieved inventory should not be null");
    Long actualInventoryId = ((Number) inventoryDto.get("inventoryId")).longValue();
    assertThat(actualInventoryId).isEqualTo(expectedInventoryId);
    System.out.println("The expected Id is: " + expectedInventoryId);
  }

  @Test
  @DisplayName("Test to get all inventories success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetAllInventoriesSuccess() throws Exception {

    // Create multiple inventories
    List<CreateInventoryDto> inventoryDtoList = createInventoryDtoList(5);
    for (CreateInventoryDto createInventoryDto : inventoryDtoList) {
      createInventory(createInventoryDto);
    }

    // Now get all inventories
    var url = "http://localhost:" + port + baseUrl + "/inventory";

    var response = webTestClient.get().uri(url)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getMessage()).isEqualTo("All inventories retrieved successfully");
  }

  @Test
  @DisplayName("Test to get inventory by product id success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetInventoryByProductIdSuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100), // Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today
        LocalDate.now().plusDays(365) // Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the productId from the saved inventory
    UUID expectedProductId = UUID.fromString((String) savedInventory.get("productId"));

    // Now get the inventory by product id
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/product/" + expectedProductId;

    var response = webTestClient.get().uri(url)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertInstanceOf(Map.class, response.getData());

    @SuppressWarnings("unchecked")
    Map<String, Object> inventoryDto = (Map<String, Object>) response.getData();
    assertNotNull(inventoryDto, "Retrieved inventory should not be null");
    UUID actualProductId = UUID.fromString((String) inventoryDto.get("productId"));
    assertThat(actualProductId).isEqualTo(expectedProductId);
  }

  @Test
  @DisplayName("Test get inventory by product id with no inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetInventoryByProductIdNotFound() throws Exception {
    // Use a random UUID that does not exist in the database
    UUID nonExistentProductId = UUID.randomUUID();

    // Now get the inventory by product id
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/product/" + nonExistentProductId;

    var response = webTestClient.get().uri(url)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getMessage()).isEqualTo("Product not found with id: " + nonExistentProductId);

  }

  @Test
  @DisplayName("Test to update inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testUpdateInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100), // Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today
        LocalDate.now().plusDays(365)// Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto); //inventoryDto

    // Extract the inventoryId from the saved inventory
    long inventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Prepare the update data
    var updateInventoryDto = new UpdateInventoryDto(

        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
        faker.number().numberBetween(1, 100), // Randomly generate a new reserved quantity
        faker.number().numberBetween(1, 100) // Randomly generate a new available stock
    );
    // Now update the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/update/" + inventoryId;

    var response = webTestClient.put().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateInventoryDto)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getMessage()).isEqualTo("Inventory updated successfully");


  }

  @Test
  @DisplayName("Test to delete inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testDeleteInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100),// Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today
        LocalDate.now().plusDays(365) // Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the inventoryId from the saved inventory
    long inventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Now delete the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/" + inventoryId;

    var response = webTestClient.method(HttpMethod.DELETE).uri(url)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getMessage()).isEqualTo("Inventory deleted successfully");
  }

  @Test
  @DisplayName("Test bulk delete inventories success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testBulkDeleteInventoriesSuccess() throws Exception {
    // Create multiple inventories
    List<CreateInventoryDto> inventoryDtoList = createInventoryDtoList(5);
    List<Long> inventoryIds = new ArrayList<>();
    for (CreateInventoryDto createInventoryDto : inventoryDtoList) {
      Map<String, Object> savedInventory = createInventory(createInventoryDto);
      inventoryIds.add(Long.parseLong(savedInventory.get("inventoryId").toString()));
    }

    // Now bulk delete the inventories
    var url = "http://localhost:" + port + baseUrl + "/inventory/bulk-delete";
    var response = webTestClient.method(HttpMethod.DELETE).uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(inventoryIds)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assertThat(response).isNotNull();
  }

  @Test
  @DisplayName("Test to deduct inventory reactively success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testDeductInventorySuccess() throws Exception {

    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100), // Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today
        LocalDate.now().plusDays(365) // Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Prepare the deduction request
    var deductInventoryRequestDto = new DeductInventoryRequestDto(
        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
            /*
             * // Randomly generate a quantity to deduct may generate a number greater than the available stock
             * //it is good to give it a small value like 1 to avoid the error
             *
             */
//        faker.number().numberBetween(1, 10) // Randomly generate a quantity to deduct
            1
    );

    // Now deduct the inventory
    /*
     * NOTE: This endpoint is for reactive programming, so it returns a Mono<Result>,
     * RestTemplate can also be used to test it.
     */
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/deduct-inventory-reactive";

    var response = webTestClient.method(HttpMethod.PATCH).uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(deductInventoryRequestDto)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getMessage()).isEqualTo("Inventory deducted successfully");

  }

  @Test
  @DisplayName("Test restore inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testRestoreInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100),// Randomly generate a reserved quantity
            LocalDate.now(), // Set manufactured date to today and before expiry date
        LocalDate.now().plusDays(365) // Set expiry date to 30 days in the future

    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Prepare the restore request
    var restoreInventoryDto = new RestoreInventoryDto(
        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
        faker.number().numberBetween(1, 10) // Randomly generate a quantity to restore
    );

    // Now restore the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/restore-inventory";
    var response = webTestClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(restoreInventoryDto)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response).isNotNull();
    assertThat(response.getMessage()).isEqualTo("Inventory restored successfully");
  }

}
