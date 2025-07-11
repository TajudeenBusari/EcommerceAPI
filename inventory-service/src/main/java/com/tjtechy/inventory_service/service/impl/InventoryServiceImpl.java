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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
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
   * This method is used to get inventory by id and cached for performance.
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

  /**
   * This method is used to get inventory by product id and cached for performance.
   * @param productId
   * @return Inventory
   */
  @Cacheable(value = "inventoryByProductId", key = "#productId")
  @Override
  public Inventory getInventoryByProductId(UUID productId) {
    var foundInventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    return foundInventory;
  }

  /**
   * This method is used to get all inventories and cached for performance.
   * @return List of Inventory
   */
  @Cacheable(value = "inventories")
  @Override
  public List<Inventory> getAllInventory() {
    return inventoryRepository.findAll();
  }

  /**
   * Updates an existing inventory record.
   * <p>
   *   This method retrieves the inventory record for the specified ID, updates its available stock and
   *   reserved quantity, and then saves the updated record back to the database.
   * </p>
   * @param inventoryId the ID of the inventory to update
   * @param inventory the inventory object containing updated values
   * @return the updated inventory object
   * @throws InventoryNotFoundException if no inventory is found with the specified ID
   */
  @CachePut(value = "inventory", key = "#inventoryId")
  @Override
  public Inventory updateInventory(Long inventoryId, Inventory inventory) {

    var foundInventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new InventoryNotFoundException(inventoryId));

    foundInventory.setAvailableStock(inventory.getAvailableStock());
    foundInventory.setReservedQuantity(inventory.getReservedQuantity());

    return inventoryRepository.save(foundInventory);
  }

  /**
   * Deletes an inventory by its ID.
   * <p>
   *   This method retrieves the inventory record for the specified ID and deletes it from the database.
   *   If the inventory with the given ID does not exist, it throws an {@link InventoryNotFoundException}.
   * </p>
   * @param inventoryId the ID of the inventory to delete
   * @throws InventoryNotFoundException if no inventory is found with the specified ID
   */
  @Override
  public void deleteInventory(Long inventoryId) {
    var foundInventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new InventoryNotFoundException(inventoryId));
    inventoryRepository.delete(foundInventory);
  }

  /**
   * Deletes multiple inventories by their IDs.
   * <p>
   *   This method retrieves all inventory records for the specified list of IDs, deletes them from the
   *   database, and throws an {@link InventoryNotFoundException} if any of the provided IDs do not exist.
   * </p>
   * @param inventoryIds the list of inventory IDs to delete
   * @throws InventoryNotFoundException if any of the provided inventory IDs do not exist
   */
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

  /**
   * Restores the specified quantity of inventory stock for a given product.
   * <p>
   *   This method retrieves the inventory record for the specified product ID, updates the available stock.
   *   by adding the quantity to restore, and decreases the reserved quantity by the same amount.
   *   It is typically used when an order is cancelled, returned/deleted, or updated and
   *   the inventory needs to be restored.
   * </p>
   * @param productId the ID of the product for which to restore inventory
   * @param quantityToRestore the quantity to restore to the inventory
   * @throws ProductNotFoundException if the product ID does not exist in the inventory
   */
  @Override
  public void restoreInventoryStock(UUID productId, Integer quantityToRestore) {
    var foundInventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    //Add the quantity back to available stock
    var updatedAvailableStock = foundInventory.getAvailableStock() + quantityToRestore;
    //update the reserved quantity
    foundInventory.setReservedQuantity(foundInventory.getReservedQuantity() - quantityToRestore);
    foundInventory.setAvailableStock(updatedAvailableStock);
    inventoryRepository.save(foundInventory);
    logger.info("Restored {} units of inventory for productId: {}", quantityToRestore, productId);

  }

}
