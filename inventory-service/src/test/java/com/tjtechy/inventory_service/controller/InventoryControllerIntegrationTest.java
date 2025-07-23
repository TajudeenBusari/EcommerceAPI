/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.tjtechy.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

public class InventoryControllerIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  private final Faker faker = new Faker(); //initialize Faker to generate random data

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
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
          faker.number().numberBetween(1, 100)// Randomly generate available stock

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

      var headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      var request = new HttpEntity<>(objectMapper.writeValueAsString(createInventoryDto), headers);
      var response = restTemplate.postForEntity(url, request, String.class);
      assertThat(response.getStatusCodeValue()).isEqualTo(200);
      Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
      Map<String, Object> savedInventory = (Map<String, Object>) responseBody.get("data");
      assertNotNull(savedInventory, "Saved inventories should not be null");
      return savedInventory;
  }

  @Test
  @DisplayName("Test to create inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testCreateInventorySuccess() throws Exception {
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
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
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the inventoryId from the saved inventory
    Long expectedInventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Now get the inventory by id
    var url = "http://localhost:" + port + baseUrl + "/inventory/" + expectedInventoryId;
    var response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> inventoryDto = (Map<String, Object>) responseBody.get("data");
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
    var response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    //assert the response message
    assertThat(response.getBody()).contains("All inventories retrieved successfully");
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    List<Map<String, Object>> inventoryDtos = (List<Map<String, Object>>) responseBody.get("data");
    assertNotNull(inventoryDtos, "Retrieved inventories should not be null");
    assertFalse(inventoryDtos.isEmpty(), "Retrieved inventories should not be empty");
  }

  @Test
  @DisplayName("Test to get inventory by product id success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testGetInventoryByProductIdSuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the productId from the saved inventory
    UUID expectedProductId = UUID.fromString((String) savedInventory.get("productId"));

    // Now get the inventory by product id
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/product/" + expectedProductId;
    var response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> inventoryDto = (Map<String, Object>) responseBody.get("data");
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
    var response = restTemplate.getForEntity(url, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(404);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("message")).isEqualTo("Product not found with id: " + nonExistentProductId);
  }

  @Test
  @DisplayName("Test to update inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testUpdateInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto); //inventoryDto

    // Extract the inventoryId from the saved inventory
    Long inventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Prepare the update data
    var updateInventoryDto = new UpdateInventoryDto(

        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
        faker.number().numberBetween(1, 100), // Randomly generate a new reserved quantity
        faker.number().numberBetween(1, 100) // Randomly generate a new available stock
    );
    // Now update the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/update/" + inventoryId;
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var request = new HttpEntity<>(objectMapper.writeValueAsString(updateInventoryDto), headers);
    var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, request, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    Map<String, Object> updatedInventoryDto = (Map<String, Object>) responseBody.get("data");
    assertNotNull(updatedInventoryDto, "Updated inventory should not be null");
    Long updatedInventoryId = ((Number) updatedInventoryDto.get("inventoryId")).longValue();
    assertThat(updatedInventoryId).isEqualTo(inventoryId);
  }

  @Test
  @DisplayName("Test to delete inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testDeleteInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Extract the inventoryId from the saved inventory
    Long inventoryId = Long.parseLong(savedInventory.get("inventoryId").toString());

    // Now delete the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/" + inventoryId;
    var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, null, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("message")).isEqualTo("Inventory deleted successfully");
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
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var request = new HttpEntity<>(objectMapper.writeValueAsString(inventoryIds), headers);
    var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, request, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("message")).isEqualTo("Inventories deleted successfully");
  }

  @Test
  @DisplayName("Test to deduct inventory reactively success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testDeductInventorySuccess() throws Exception {

    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Prepare the deduction request
    var deductInventoryRequestDto = new DeductInventoryRequestDto(
        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
            /**
             * // Randomly generate a quantity to deduct may generate a number greater than the available stock
             * //it is good to give it a small value like 1 to avoid the error
             *
             */
//        faker.number().numberBetween(1, 10) // Randomly generate a quantity to deduct
            1
    );

    // Now deduct the inventory
    /**
     * NOTE: This endpoint is for reactive programming, so it returns a Mono<Result>,
     * RestTemplate can also be used to test it.
     */
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/deduct-inventory-reactive";
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var request = new HttpEntity<>(objectMapper.writeValueAsString(deductInventoryRequestDto), headers);
    var response = restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("message")).isEqualTo("Inventory deducted successfully");
  }

  @Test
  @DisplayName("Test restore inventory success")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  public void testRestoreInventorySuccess() throws Exception {
    // Create an inventory first
    var createInventoryDto = new CreateInventoryDto(
        UUID.randomUUID(), // Randomly generate a productId
        faker.number().numberBetween(1, 100), // Randomly generate a quantity between 1 and 100
        faker.number().numberBetween(1, 100) // Randomly generate a reserved quantity
    );
    Map<String, Object> savedInventory = createInventory(createInventoryDto);

    // Prepare the restore request
    var restoreInventoryDto = new RestoreInventoryDto(
        UUID.fromString((String) savedInventory.get("productId")), // Use the same productId
        faker.number().numberBetween(1, 10) // Randomly generate a quantity to restore
    );

    // Now restore the inventory
    var url = "http://localhost:" + port + baseUrl + "/inventory/internal/restore-inventory";
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var request = new HttpEntity<>(objectMapper.writeValueAsString(restoreInventoryDto), headers);
    var response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    assertThat(response.getStatusCodeValue()).isEqualTo(200);

    Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
    assertThat(responseBody.get("message")).isEqualTo("Inventory restored successfully");
  }

}
