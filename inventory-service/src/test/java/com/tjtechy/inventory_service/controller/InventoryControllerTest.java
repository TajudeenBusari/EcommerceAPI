/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.tjtechy.*;
import com.tjtechy.inventory_service.exception.ExceptionHandlingAdvice;
import com.tjtechy.inventory_service.service.InventoryService;
import com.tjtechy.modelNotFoundException.InventoryNotFoundException;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = InventoryController.class)
@AutoConfigureMockMvc
/**
 * So @ContextConfiguration(classes = {...}) forces Spring to load only what you need,
 * overriding the default component scan or @SpringBootApplication class
 * In @WebMvcTest, adding @ContextConfiguration(classes = {InventoryController.class}) helps you:
 * Explicitly isolate the controller
 * Prevent JPA and Redis config from being loaded
 * Speed up your tests and avoid irrelevant bean creation errors
 */
@ContextConfiguration(classes = {InventoryController.class})
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none",
        "spring.cloud.config.enabled=false", // Disable Spring Cloud Config
        "eureka.client.enabled=false", // Disable Eureka Client
        "spring.redis.enabled=false", // Disable Redis

})
@Import(ExceptionHandlingAdvice.class) // Import the ExceptionHandlingAdvice class to handle exceptions in the controller tests

/**
 * Use @WebMvcTest(YourController.class) for clean controller unit tests.
 * Add @ContextConfiguration(classes = {...}) only if you want to fully control what beans are loaded and avoid config scanning pitfalls.
 * Avoid @SpringBootTest unless you're writing a full integration test.
 *
 */
class InventoryControllerTest {

  @MockitoBean
  private InventoryService inventoryService;

  @MockitoBean
  private RedisConnectionFactory redisConnectionFactory;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  private List<InventoryDto> inventoryDtoList;

  private List<Inventory> inventoryList;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  private WebTestClient webTestClient; // Use WebTestClient for reactive testing

  @BeforeEach
  void setUp() {
    Faker faker = new Faker();

    //create a list of InventoryDto using faker object
    inventoryDtoList = new ArrayList<>();//initialize the list to empty list avoid NullPointerException
    for (int i = 0; i < 10; i++) {
      var inventoryDto = new InventoryDto(
              faker.number().numberBetween(1L, 100L),
              UUID.randomUUID(),
              faker.number().randomDigit(),
              1// reserved quantity
      );
      inventoryDtoList.add(inventoryDto);
    }

    //create a list of Inventory using faker object
    inventoryList = new ArrayList<>();//initialize the list to empty list avoid NullPointerException
    for (int i = 0; i < 10; i++) {
      var inventory = new Inventory();
      inventory.setInventoryId(faker.number().numberBetween(1L, 100L));
      inventory.setProductId(UUID.randomUUID());
      inventory.setAvailableStock(faker.number().randomDigit());
      inventory.setReservedQuantity(faker.number().randomDigit());
      inventoryList.add(inventory);
    }


  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testAddInventorySuccess() throws Exception {
    //Given
    var createInventoryDto = new CreateInventoryDto(
            UUID.randomUUID(),
            10,
            5,
            LocalDate.now().plusYears(1)
    );
    var json = objectMapper.writeValueAsString(createInventoryDto);

    //mock the inventoryService.createInventory method
    var inventory = inventoryList.get(0);
    inventory.setProductId(createInventoryDto.productId());
    inventory.setAvailableStock(createInventoryDto.availableStock());
    inventory.setReservedQuantity(createInventoryDto.reservedQuantity());

    when(inventoryService.createInventory(any(Inventory.class)))
            .thenReturn(inventory);

    //When and then
    mockMvc.perform(post(baseUrl + "/inventory/internal/create")
            .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(jsonPath("$.message").value("Inventory created successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").value(createInventoryDto.productId().toString()))
            .andExpect(jsonPath("$.data.reservedQuantity").value(createInventoryDto.reservedQuantity()));
  }

  @Test
  void testGetInventoryByInventoryId() throws Exception {
    //Given
    var inventory = inventoryList.get(0);
    var inventoryId = inventory.getInventoryId();
    var inventoryDto = new InventoryDto(
            inventory.getInventoryId(),
            inventory.getProductId(),
            inventory.getReservedQuantity(),
            inventory.getAvailableStock()
    );

    //mock the inventoryService.getInventoryByInventoryId method
    when(inventoryService.getInventoryByInventoryId(inventoryId))
            .thenReturn(inventory);
    //When and then
    mockMvc.perform(get(baseUrl + "/inventory/" + inventoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Inventory retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.inventoryId").value(inventoryId))
            .andExpect(jsonPath("$.data.productId").value(inventoryDto.productId().toString()))
            .andExpect(jsonPath("$.data.reservedQuantity").value(inventoryDto.reservedQuantity()));

  }

  @Test
  void testGetInventoryByInventoryIdNotFound() throws Exception {
    //Given
    var inventoryId = 1L;
    //mock the inventoryService.getInventoryByInventoryId method
    when(inventoryService.getInventoryByInventoryId(inventoryId))
            .thenThrow(new InventoryNotFoundException(inventoryId));

    //When and then
    mockMvc.perform(get(baseUrl + "/inventory/" + inventoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Inventory not found with id: " + inventoryId))
            .andExpect(jsonPath("$.flag").value(false));
  }

  @Test
  void testGetAllInventorySuccess() throws Exception {
    //Given
    /**
     *This step is to ensure that the inventoryDtoList and inventoryList
     * at index 0 are same for the to pass at validation
     *
     */
    inventoryDtoList.set(0, new InventoryDto(
            inventoryList.get(0).getInventoryId(),
            inventoryList.get(0).getProductId(),
            inventoryList.get(0).getReservedQuantity(),
            inventoryList.get(0).getAvailableStock()
            ));

    //mock the inventoryService.getAllInventory method
    when(inventoryService.getAllInventory())
            .thenReturn(inventoryList);

    //When and then
    mockMvc.perform(get(baseUrl + "/inventory")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("All inventories retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].inventoryId").value(inventoryDtoList.get(0).inventoryId()))
            .andExpect(jsonPath("$.data[0].productId").value(inventoryDtoList.get(0).productId().toString()))
            .andExpect(jsonPath("$.data[0].reservedQuantity").value(inventoryDtoList.get(0).reservedQuantity()));
  }

  @Test
  void testGetInventoryByProductId() throws Exception {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var inventoryDto = new InventoryDto(
            inventory.getInventoryId(),
            inventory.getProductId(),
            inventory.getReservedQuantity(),
            inventory.getAvailableStock()
    );

    when(inventoryService.getInventoryByProductId(productId)).thenReturn(inventory);
    //When and //Then
    mockMvc.perform(get(baseUrl + "/inventory/internal/product/" + productId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Inventory with productId: " + inventoryDto.productId() + " retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.inventoryId").value(inventoryDto.inventoryId()))
            .andExpect(jsonPath("$.data.productId").value(inventoryDto.productId().toString()))
            .andExpect(jsonPath("$.data.reservedQuantity").value(inventoryDto.reservedQuantity()));
  }

  @Test
  void testGetInventoryByProductIdNotFound() throws Exception {
    //Given
    var productId = UUID.randomUUID();
    //mock the inventoryService.getInventoryByProductId method
    when(inventoryService.getInventoryByProductId(productId))
            .thenThrow(new ProductNotFoundException(productId));

    //When and then
    mockMvc.perform(get(baseUrl + "/inventory/internal/product/" + productId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Product not found with id: " + productId))
            .andExpect(jsonPath("$.flag").value(false));
  }

  @Test
  void testUpdateInventorySuccess() throws Exception {
    //Given
    var updatedInventory = inventoryList.get(0);
    var inventoryId = updatedInventory.getInventoryId();
    var updateInventoryDto = new UpdateInventoryDto(
            updatedInventory.getProductId(),
            updatedInventory.getAvailableStock(),
            updatedInventory.getReservedQuantity()
    );
    var json = objectMapper.writeValueAsString(updateInventoryDto);

    //DATA that will be returned after the update
    var inventoryDto = new InventoryDto(
            updatedInventory.getInventoryId(),
            updatedInventory.getProductId(),
            updatedInventory.getReservedQuantity(),
            updatedInventory.getAvailableStock()
    );
    when(inventoryService.updateInventory(eq(inventoryId), any(Inventory.class)))
            .thenReturn(updatedInventory);
    //When and then
    mockMvc.perform(put(baseUrl + "/inventory/internal/update/" + inventoryId)
            .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(jsonPath("$.message").value("Inventory updated successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.inventoryId").value(inventoryDto.inventoryId()))
            .andExpect(jsonPath("$.data.productId").value(inventoryDto.productId().toString()))
            .andExpect(jsonPath("$.data.reservedQuantity").value(inventoryDto.reservedQuantity()));
  }

  @Test
  void testDeleteInventorySuccess() throws Exception {
    //Given
    var inventoryId = inventoryList.get(0).getInventoryId();

    //mock the inventoryService.deleteInventory method
    doNothing().when(inventoryService).deleteInventory(inventoryId);

    //When and then
    mockMvc.perform(delete(baseUrl + "/inventory/" + inventoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Inventory deleted successfully"))
            .andExpect(jsonPath("$.flag").value(true));

  }

  @Test
  void testBulkDeleteInventoriesSuccess() throws Exception {
    //Given
    var inventoryId1 = inventoryList.get(0).getInventoryId();
    var inventoryId2 = inventoryList.get(1).getInventoryId();
    var inventoryId3 = inventoryList.get(2).getInventoryId();
    var inventoryIds = List.of(inventoryId1, inventoryId2, inventoryId3);

    //mock the inventoryService.bulkDeleteInventoriesByInventoryId method
    doNothing().when(inventoryService).bulkDeleteInventoriesByInventoryId(inventoryIds);

    //When and then
    mockMvc.perform(delete(baseUrl + "/inventory/bulk-delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(inventoryIds)))
            .andExpect(jsonPath("$.message").value("Inventories deleted successfully"))
            .andExpect(jsonPath("$.flag").value(true));

  }

  @Test
  void testBulkDeleteInventoriesNotFound() throws Exception {
    //Given
    var inventoryId1 = inventoryList.get(0).getInventoryId();
    var inventoryId2 = inventoryList.get(1).getInventoryId();
    var inventoryId3 = inventoryList.get(2).getInventoryId();
    var inventoryIds = List.of(inventoryId1, inventoryId2, inventoryId3);

    //mock the inventoryService.bulkDeleteInventoriesByInventoryId method
    doThrow(new InventoryNotFoundException(inventoryIds)).when(inventoryService).bulkDeleteInventoriesByInventoryId(inventoryIds);

    //When and then
    mockMvc.perform(delete(baseUrl + "/inventory/bulk-delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(inventoryIds)))
            .andExpect(jsonPath("$.message").value("Inventory not found with ids: " + inventoryIds))
            .andExpect(jsonPath("$.flag").value(false));

  }

  @Test
  void testDeductInventorySuccess() throws Exception {
    //Given
    var deductInventoryDto = new DeductInventoryRequestDto(
            UUID.randomUUID(),
            5
    );
    var inventory = inventoryList.get(0);
    inventory.setProductId(deductInventoryDto.productId());
    inventory.setReservedQuantity(deductInventoryDto.quantity());
    //mock the inventoryService.deductInventory method
    doNothing().when(inventoryService).deductInventory(inventory.getProductId(), inventory.getReservedQuantity());
    //When and then
    mockMvc.perform(patch(baseUrl + "/inventory/internal/deduct-inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(deductInventoryDto)))
            .andExpect(jsonPath("$.message").value("Inventory deducted successfully"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  /**
   * For reactive endpoint testing, we use WebTestClient.
   * The WebTestClient is a non-blocking, reactive client for testing web applications
   * which is part of the Spring WebFlux module.
   */
  @Test
  void testDeductInventoryReactiveSuccess() throws Exception {
    //Given
    var deductInventoryDto = new DeductInventoryRequestDto(
            UUID.randomUUID(),
            5
    );

    var json = objectMapper.writeValueAsString(deductInventoryDto);
    var inventory = inventoryList.get(0);
    inventory.setProductId(deductInventoryDto.productId());
    inventory.setReservedQuantity(deductInventoryDto.quantity());

    //mock the inventoryService.deductInventoryReactive method
    when(inventoryService.deductInventoryReactive(deductInventoryDto.productId(), deductInventoryDto.quantity()))
            .thenReturn(Mono.empty());
    //When and then
    webTestClient.patch()
            .uri(baseUrl+ "/inventory/internal/deduct-inventory-reactive")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Inventory deducted successfully")
            .jsonPath("$.flag").isEqualTo(true);
  }
}