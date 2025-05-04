/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.service.impl;

import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.tjtechy.inventory_service.repository.InventoryRepository;
import com.tjtechy.Inventory;
import com.tjtechy.inventory_service.service.InventoryService;
import com.tjtechy.modelNotFoundException.InventoryNotFoundException;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

  private final InventoryRepository inventoryRepository;
  public InventoryServiceImpl(InventoryRepository inventoryRepository) {
    this.inventoryRepository = inventoryRepository;
  }

  /**
   * This method is used to create a new inventory.
   *This method is triggered when a new product is added to the system.
   * @param inventory the inventory to be created
   * @return the created inventory
   */
  @Override
  @CachePut(value = "inventory", key = "#inventory.productId")
  public Inventory createInventory(Inventory inventory) {
    
    //1. validate the inventory object
    if (inventory.getProductId() == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }

    //2. check if the available stock is less than 0
    if (inventory.getAvailableStock() < 0) {
      throw new IllegalArgumentException("Available stock cannot be less than 0");
    }
    //3. check if the inventory already exists in the database for the given product ID
    Optional<Inventory> existingInventory = inventoryRepository.findByProductId(inventory.getProductId());
    if (existingInventory.isPresent()) {
      throw new IllegalArgumentException("Inventory already exists for this product ID");
    }
    //4. save the inventory to the database
    var savedInventory = inventoryRepository.save(inventory);

    return savedInventory;
  }

  /**
   * This method is used to get inventory by id
   * @param inventoryId
   * @return Inventory
   */
  @Cacheable(value = "inventoryByInventoryId", key = "#inventoryId")
  @Override
  public Inventory getInventoryByInventoryId(Long inventoryId) {
    var foundInventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    return foundInventory;
  }

  @Cacheable(value = "inventoryByProductId", key = "#productId")
  @Override
  public Inventory getInventoryByProductId(UUID productId) {
    var foundInventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    return foundInventory;
  }

  @Cacheable(value = "inventories")
  @Override
  public List<Inventory> getAllInventory() {
    return inventoryRepository.findAll();
  }

  @CachePut(value = "inventory", key = "#inventoryId")
  @Override
  public Inventory updateInventory(Long inventoryId, Inventory inventory) {

    var foundInventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

    foundInventory.setAvailableStock(inventory.getAvailableStock());
    foundInventory.setReservedQuantity(inventory.getReservedQuantity());

    return inventoryRepository.save(foundInventory);
  }

  @Override
  public void deleteInventory(Long inventoryId) {
    var foundInventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    inventoryRepository.delete(foundInventory);
  }

  @Override
  public void bulkDeleteInventoriesByInventoryId(List<Long> inventoryIds) {

    var inventories = inventoryRepository.findAllById(inventoryIds);
    //extract existing inventory ids
    var foundInventories = inventories.stream()
            .map(Inventory::getInventoryId).toList();

    //collect the ids that are not found
    var notFoundInventoryIds = inventoryIds.stream()
            .filter(id -> !foundInventories.contains(id))
            .toList();
    //delete the found inventories
    if (!inventories.isEmpty()){
      inventoryRepository.deleteAll(inventories);
    }
    //throw exception if there are missing ids
    if (!notFoundInventoryIds.isEmpty()) {
      throw new InventoryNotFoundException(notFoundInventoryIds);
    }
  }


  /**
   * It is a non-reactive operation that deducts the specified quantity from the
   * inventory stock for a given product.
   * <p>
   *   This method retrieves the inventory record for the specified product ID, checks if there is
   *   sufficient stock available, and then updates the inventory by deducting the requested
   *   quantity from the available stock, while also increasing the reserved quantity by the same amount.
   * </p>
   * @param productId the ID of the product for which to deduct inventory
   * @param quantity the quantity to deduct from the inventory
   * @throws ProductNotFoundException if the product ID does not exist in the inventory
   * @throws InsufficientStockQuantityException if the available stock is less than the requested quantity
   */
  @Override
  public void deductInventory(UUID productId, Integer quantity) {
    var foundInventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    if (foundInventory.getAvailableStock() < quantity) {
      throw new InsufficientStockQuantityException(productId);
    }

    foundInventory.setAvailableStock(foundInventory.getAvailableStock() - quantity);
    foundInventory.setReservedQuantity(foundInventory.getReservedQuantity() + quantity);
    inventoryRepository.save(foundInventory);
  }

  /**
   * Deducts the specified quantity from the inventory stock for a given product in a reactive way.
   * <p>
   *   This method retrieve the inventory record for the specified product ID, checks if there is
   *   sufficient stock available, and then updates the inventory by deducting the requested
   *   quantity from the available stock, while also increasing the reserved quantity by the same amount.
   *   By default, the reserved quantity is set to 1, so reserved amount will just increase by the quantity
   *   number.
   *   The operation is performed in a NON-BLOCKING manner using Project Reactor's Mono suitable for
   *   microservices architecture.
   * </p>
   * @param productId the ID of the product for which to deduct inventory
   * @Param quantity the quantity to deduct from the inventory
   * @return a {@link Mono} that completes when the inventory has been successfully deducted or
   * emits an error if operation fails
   * @throws ProductNotFoundException if the product ID does not exist in the inventory
   * @throws InsufficientStockQuantityException if the available stock is less than the requested quantity
   *
   */
  @Override
  public Mono<Void> deductInventoryReactive(UUID productId, Integer quantity) {
    return Mono.fromCallable(() -> inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId)))
            .flatMap(foundInventory -> {
              if (foundInventory.getAvailableStock() < quantity) {
                throw new InsufficientStockQuantityException(productId);
              }
              foundInventory.setAvailableStock(foundInventory.getAvailableStock() - quantity);
              foundInventory.setReservedQuantity(foundInventory.getReservedQuantity() + quantity);
              return Mono.fromCallable(() -> inventoryRepository.save(foundInventory));
            }).then(); //convert to Mono<Void>

  }

//  @Override
//  public void deleteInventoryByProductId(UUID productId) {
//    var foundInventory = inventoryRepository.findByProductId(productId)
//            .orElseThrow(() -> new ProductNotFoundException(productId));
//    inventoryRepository.delete(foundInventory);
//  }
}
