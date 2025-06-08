/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.tjtechy.CreateInventoryDto;
import com.tjtechy.DeductInventoryRequestDto;
import com.tjtechy.InventoryDto;
import com.tjtechy.UpdateInventoryDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //prevents the test from using an embedded database
@AutoConfigureMockMvc //enables MockMvc for testing the controller
@Tag("InventoryControllerIntegrationTest")
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.application.name=inventory-service",
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
})

public class InventoryControllerIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  private WebTestClient webClient;

  private List<InventoryDto> inventoryDtoList;

  @Container
  private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
      registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
      registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
      registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
  }

  @BeforeAll
  static void setUp() {
      // Start the PostgreSQL container
      postgreSQLContainer.start();

      // Wait for the container to be ready
      while (!postgreSQLContainer.isRunning()) {
          try {
              Thread.sleep(1000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
      }
  }

  @AfterAll
  static void tearDown() {
      // Stop the PostgreSQL container
      postgreSQLContainer.stop();
  }
  @BeforeEach
  void setUpEach() throws Exception {
    Faker faker = new Faker();

    //create a list of InventoryDto object
    inventoryDtoList = new ArrayList<>(); //initialize the list to empty to avoid NullPointerException

    //create a list of CreateInventoryDto object
    for(int i = 0; i<5; i++){
      var createInventoryDto = new CreateInventoryDto(
              UUID.randomUUID(),
              faker.number().numberBetween(1, 100),
              faker.number().numberBetween(1, 100)
      );
      //since the return type of the createInventory method is InventoryDto, we can use it to create the inventory.
      //the value here: createInventory(createInventoryDto) is InventoryDto
      //It is then added to the inventoryDtoList
      InventoryDto createdInventoryDto;

      try {
        createdInventoryDto = createInventory(createInventoryDto);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create inventory for productId: " +
                createInventoryDto.productId(), e);
      }
      inventoryDtoList.add(createdInventoryDto);
    }

  }

  //method to generate a list inventory
  private InventoryDto createInventory(CreateInventoryDto createInventoryDto) throws Exception {
    var json = objectMapper.writeValueAsString(createInventoryDto);

    var result = mockMvc.perform(post(baseUrl + "/inventory/internal/create")
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventory created successfully"))
            .andReturn();
    var responseContent = result.getResponse().getContentAsString();
    var dataResponse = objectMapper.readTree(responseContent);
    //if for some reason, an inventory is already for a productId, we can throw an exception
    if (!dataResponse.get("flag").asBoolean()) {
      throw new RuntimeException("Failed to create inventory: " + dataResponse.get("message").asText());
    }

    return objectMapper.readValue(dataResponse.get("data").toString(), InventoryDto.class);
  }

  @Test
  @DisplayName("Check Inventory Exists in setup")
  void testInventoryExists(){
    assertFalse(inventoryDtoList.isEmpty(), "Inventory list should not be empty");
    assertNotNull(inventoryDtoList.get(0).productId());
  }
  @Test
  @DisplayName("Check Add Inventory (POST) success")
  void testCreateInventorySuccess() throws Exception {
    var createInventoryDto = new CreateInventoryDto(
            UUID.randomUUID(),
            10,
            5
    );
    var result = createInventory(createInventoryDto);
    var inventoryId = result.inventoryId();
    assertNotNull(inventoryId);
    System.out.println("Inventory ID: " + inventoryId);
  }

  @Test
  @DisplayName("Check Get Inventory by ID (GET) success")
  void testGetInventoryByIdSuccess() throws Exception {
    var inventoryId = inventoryDtoList.get(0).inventoryId();
    var result = mockMvc.perform(get(baseUrl + "/inventory/" + inventoryId))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventory retrieved successfully"))
            .andExpect(jsonPath("$.data.inventoryId").value(inventoryId))
            .andReturn();
  }

  @Test
  @DisplayName("Check Get All Inventories (GET) success")
  void testGetAllInventoriesSuccess() {
    var inventories = inventoryDtoList;
    assertNotNull(inventories);
    System.out.println("Inventory List: " + inventories);
  }

  @Test
  @DisplayName("Check Get Inventory by Product ID (GET)")
  void testGetInventoryByProductIdSuccess() throws Exception {
    var productId = inventoryDtoList.get(0).productId();
    var result = mockMvc.perform(get(baseUrl + "/inventory/internal/product/" + productId))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventory with productId: " + productId + " retrieved successfully"))
            .andExpect(jsonPath("$.data.productId").value(productId.toString()))
            .andReturn();
  }

  @Test
  @DisplayName("Check Update Inventory (PUT) success")
  void testUpdateInventorySuccess()throws Exception {
    var productId = inventoryDtoList.get(0).productId();
    var inventoryId = inventoryDtoList.get(0).inventoryId();
    var updateInventoryDto = new UpdateInventoryDto(
            productId,
            20,
            10
    );
    var json = objectMapper.writeValueAsString(updateInventoryDto);
    var result = mockMvc.perform(put(baseUrl + "/inventory/internal/update/" + inventoryId)
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventory updated successfully"))
            .andExpect(jsonPath("$.data.productId").value(productId.toString()))
            .andExpect(jsonPath("$.data.reservedQuantity").value(10))
            .andExpect(jsonPath("$.data.inventoryId").value(inventoryId))
            .andReturn();
  }

  @Test
  @DisplayName("Check Delete Inventory (DELETE) success")
  void testDeleteInventorySuccess() throws Exception {
    var inventoryId = inventoryDtoList.get(0).inventoryId();
    mockMvc.perform(delete(baseUrl + "/inventory/" + inventoryId))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventory deleted successfully"))
            .andReturn();
  }

  @Test
  @DisplayName("Check Bulk Delete Inventories (DELETE) success")
  void testBulkDeleteInventories() throws Exception {
    var inventoryId1 = inventoryDtoList.get(0).inventoryId();
    var inventoryId2 = inventoryDtoList.get(1).inventoryId();
    var inventoryId3 = inventoryDtoList.get(2).inventoryId();
    var inventoryIds = List.of(inventoryId1, inventoryId2, inventoryId3);
    var json = objectMapper.writeValueAsString(inventoryIds);
    mockMvc.perform(delete(baseUrl + "/inventory/bulk-delete")
            .contentType(MediaType.APPLICATION_JSON).content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Inventories deleted successfully"))
            .andReturn();

  }

  @Test
  @DisplayName("Check Deduct Inventory reactively (PATCH) success")
  void testDeductInventoryReactivelySuccess() throws Exception {
    var productId = inventoryDtoList.get(0).productId();

    var deductInventoryDto = new DeductInventoryRequestDto(
            productId,
            5
    );
    var json = objectMapper.writeValueAsString(deductInventoryDto);
    var result = webClient.patch()
            .uri(baseUrl + "/inventory/internal/deduct-inventory-reactive")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("Inventory deducted successfully")
            .returnResult();
  }

}
