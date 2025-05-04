/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.service.impl;

import com.github.javafaker.Faker;
import com.tjtechy.Inventory;
import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.tjtechy.inventory_service.repository.InventoryRepository;
import com.tjtechy.modelNotFoundException.InventoryNotFoundException;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.test.StepVerifier;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
  @Mock
  private InventoryRepository inventoryRepository;

  @InjectMocks
  private InventoryServiceImpl inventoryService;

  private List<Inventory> inventoryList;


  @BeforeEach
  void setUp() {
    //create a list of inventory objects using faker
    Faker faker = new Faker();
    inventoryList = new ArrayList<>(); //initialize the list to empty list avoid NullPointerException
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
  void testCreateInventorySuccess() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findByProductId(inventory.getProductId())).thenReturn(Optional.empty());
    given(inventoryRepository.save(inventory)).willReturn(inventory);

    //When
    var createdInventory = inventoryService.createInventory(inventory);
    //Then
    assertNotNull(createdInventory);
    assertEquals(inventory.getInventoryId(), createdInventory.getInventoryId());
    verify(inventoryRepository, times(1)).save(inventory);
    verify(inventoryRepository, times(1)).findByProductId(inventory.getProductId());
  }

  @Test
  void testCreateInventoryWhenProductIdIsNull(){
    //Given
    var inventory = inventoryList.get(0);
    inventory.setProductId(null);

    //When
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      inventoryService.createInventory(inventory);
    });

    //Then
    assertEquals("Product ID cannot be null", exception.getMessage());
    verify(inventoryRepository, never()).save(inventory);
  }

  @Test
  void testCreateInventoryWhenAvailableStockIsLessThanZero(){
    //Given
    var inventory = inventoryList.get(0);
    inventory.setAvailableStock(-1);
    //When
    var exception = assertThrows(IllegalArgumentException.class, () -> {inventoryService.createInventory(inventory);});
    //Then
    assertEquals("Available stock cannot be less than 0", exception.getMessage());
    verify(inventoryRepository, never()).save(inventory);
  }

  @Test
  void testGetInventoryByInventoryIdSuccess() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));

    //When
    var foundInventory = inventoryService.getInventoryByInventoryId(inventory.getInventoryId());

    //Then
    assertNotNull(foundInventory);
    assertEquals(inventory.getInventoryId(), foundInventory.getInventoryId());
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());

  }

  @Test
  void testGetInventoryByInventoryIdNotFound(){
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.empty());

    //When
    var exception = assertThrows(
            InventoryNotFoundException.class, () -> inventoryService.getInventoryByInventoryId(inventory.getInventoryId())
    );
    //Then
    assertEquals("Inventory not found with id: " + inventory.getInventoryId(), exception.getMessage());
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
  }

  @Test
  void testGetInventoryByProductIdSuccess() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findByProductId(inventory.getProductId())).thenReturn(Optional.of(inventory));

    //When
    var foundInventory = inventoryService.getInventoryByProductId(inventory.getProductId());

    //Then
    assertNotNull(foundInventory);
    assertEquals(inventory.getProductId(), foundInventory.getProductId());
    verify(inventoryRepository, times(1)).findByProductId(inventory.getProductId());
  }

  @Test
  void testGetInventoryByProductIdNotFound(){
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findByProductId(inventory.getProductId())).thenReturn(Optional.empty());

    //When
    var exception = assertThrows(
            ProductNotFoundException.class, () -> inventoryService.getInventoryByProductId(inventory.getProductId())
    );
    //Then
    assertEquals("Product not found with id: " + inventory.getProductId(), exception.getMessage());
    verify(inventoryRepository, times(1)).findByProductId(inventory.getProductId());

  }

  @Test
  void testGetAllInventorySuccess() {
    //Given
    when(inventoryRepository.findAll()).thenReturn(inventoryList);
    //When
    var foundInventoryList = inventoryService.getAllInventory();
    //Then
    assertNotNull(foundInventoryList);
    assertEquals(10, foundInventoryList.size());
  }

  @Test
  void testUpdateInventorySuccess() {
    //Given
    var inventory = inventoryList.get(0);

    //inventory that will be saved to the database
    var updatedInventory = new Inventory();
    updatedInventory.setInventoryId(inventory.getInventoryId());
    updatedInventory.setProductId(inventory.getProductId());
    updatedInventory.setAvailableStock(10);//new value
    updatedInventory.setReservedQuantity(5);//new value

    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(inventory)).thenReturn(updatedInventory);

    //When
    var foundInventory = inventoryService.updateInventory(inventory.getInventoryId(), updatedInventory);

    //Then
    assertNotNull(foundInventory);
    assertEquals(inventory.getInventoryId(), foundInventory.getInventoryId());
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
    verify(inventoryRepository, times(1)).save(inventory);
  }

  @Test
  void testUpdateInventoryNotFound() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.empty());

    //When
    var exception = assertThrows(
            InventoryNotFoundException.class, () -> inventoryService.updateInventory(inventory.getInventoryId(), inventory)
    );
    //Then
    assertEquals("Inventory not found with id: " + inventory.getInventoryId(), exception.getMessage());
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
    verify(inventoryRepository, never()).save(inventory);
  }

  @Test
  void testDeleteInventorySuccess() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.of(inventory));
    //When
    inventoryService.deleteInventory(inventory.getInventoryId());
    //Then
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
  }

  @Test
  void testDeleteInventoryNotFound() {
    //Given
    var inventory = inventoryList.get(0);
    when(inventoryRepository.findById(inventory.getInventoryId())).thenReturn(Optional.empty());
    //When
    var exception = assertThrows(
            InventoryNotFoundException.class, () -> inventoryService.deleteInventory(inventory.getInventoryId())
    );
    //Then
    assertEquals("Inventory not found with id: " + inventory.getInventoryId(), exception.getMessage());
    verify(inventoryRepository, times(1)).findById(inventory.getInventoryId());
  }

  @Test
  void testBulkDeleteInventoriesByInventoryIdSuccess() {
    //Given
    var inventoryIds = new ArrayList<Long>();
    for (int i = 0; i < 10; i++) {
      var inventory = inventoryList.get(i);
      inventoryIds.add(inventory.getInventoryId());
    }
    when(inventoryRepository.findAllById(inventoryIds)).thenReturn(inventoryList);

    //When
    inventoryService.bulkDeleteInventoriesByInventoryId(inventoryIds);

    //Then
    verify(inventoryRepository, times(1)).findAllById(inventoryIds);
  }

  @Test
  void testBulkDeleteInventoriesWithSomeInventoryNotFound(){
    //Given
    var missingId = 100L;
    List<Long> inventoryIds  = Arrays.asList(missingId, inventoryList.get(0).getInventoryId(), inventoryList.get(1).getInventoryId());

    var foundInventories = Arrays.asList(inventoryList.get(0), inventoryList.get(1));

    when(inventoryRepository.findAllById(inventoryIds)).thenReturn(foundInventories);
    //When
    var exception = assertThrows(
            InventoryNotFoundException.class, () -> inventoryService.bulkDeleteInventoriesByInventoryId(inventoryIds)
    );
    //Then
    assertTrue(exception.getMessage().contains("Inventory not found with ids:"));//contains should just state part of the exception message
    verify(inventoryRepository, times(1)).findAllById(inventoryIds);
    verify(inventoryRepository).deleteAll(foundInventories);


  }

  @Test
  void testDeductInventorySuccess() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 5;
    inventory.setAvailableStock(15); //set a value greater than quantity
    inventory.setReservedQuantity(1); //set the initial reserved quantity
    var availableStock = inventory.getAvailableStock();
    var reservedQuantity = inventory.getReservedQuantity();
    var newAvailableStock = availableStock - quantity;
    var newReservedQuantity = reservedQuantity + quantity;

    when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(inventory)).thenReturn(inventory);

    //When
    inventoryService.deductInventory(productId, quantity);
    //Then
    assertEquals(newAvailableStock, inventory.getAvailableStock());
    assertEquals(newReservedQuantity, inventory.getReservedQuantity());
    verify(inventoryRepository, times(1)).findByProductId(productId);
    verify(inventoryRepository, times(1)).save(inventory);
  }

  @Test
  void testDeductInventoryNotFound() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 5;

    //When
    var exception = assertThrows(ProductNotFoundException.class, () -> inventoryService.deductInventory(productId, quantity));
    //Then
    assertEquals("Product not found with id: " + productId, exception.getMessage());
  }

  @Test
  void testDeductInventoryWithInsufficientStock() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 20; //greater than available stock
    inventory.setAvailableStock(15); //set a value less than quantity

    when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

    //When
    var exception = assertThrows(InsufficientStockQuantityException.class, () -> inventoryService.deductInventory(productId, quantity));
    //Then
    assertEquals("Insufficient stock quantity for product with id: " + productId, exception.getMessage());
  }

  @Test
  void testDeductInventoryReactiveSuccess() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 5;
    inventory.setAvailableStock(15); //set a value greater than quantity
    inventory.setReservedQuantity(1); //set the initial reserved quantity

    /**IMPORTANT NOTE:
     * In your unit test, both findByProductId and save should return
     * plain Java objects (Optional<Inventory> and Inventory), not Monos
     * because in the implementation class, we are wrapping synchronous
     * repository calls inside Mono.fromCallable(), which means we are turning
     * plain java method (non-reactive) into a reactive stream.
     */
    when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
    //When
    StepVerifier
            .create(inventoryService.deductInventoryReactive(productId, quantity))
            .verifyComplete();

    //Then
    assertEquals(15 - quantity, inventory.getAvailableStock());
    assertEquals(1 + quantity, inventory.getReservedQuantity());
    verify(inventoryRepository, times(1)).findByProductId(productId);
    verify(inventoryRepository, times(1)).save(inventory);
  }

  @Test
  void testDeductInventoryReactiveWhenProductNotFound() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 5;
    when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

    //When
    StepVerifier
            .create(inventoryService.deductInventoryReactive(productId, quantity))
            .expectErrorMatches(throwable -> throwable instanceof ProductNotFoundException &&
                    throwable.getMessage().equals("Product not found with id: " + productId))
            .verify();

    //Then
    verify(inventoryRepository, times(1)).findByProductId(productId);
  }

  @Test
  void testDeductInventoryReactiveWhenInsufficientStock() {
    //Given
    var inventory = inventoryList.get(0);
    var productId = inventory.getProductId();
    var quantity = 20; //greater than available stock
    inventory.setAvailableStock(15); //set a value less than quantity

    when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

    //When
    StepVerifier
            .create(inventoryService.deductInventoryReactive(productId, quantity))
            .expectErrorMatches(throwable -> throwable instanceof InsufficientStockQuantityException &&
                    throwable.getMessage().equals("Insufficient stock quantity for product with id: " + productId))
            .verify();

    //Then
    verify(inventoryRepository, times(1)).findByProductId(productId);
    verify(inventoryRepository, never()).save(inventory);
  }
}