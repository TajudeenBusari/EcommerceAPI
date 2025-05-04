/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.service;

import com.tjtechy.Inventory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

  /**
   *
   * @param inventory
   * @return
   */
  Inventory createInventory(Inventory inventory);

  /**
   *
   * @param inventoryId
   * @return
   */
  Inventory getInventoryByInventoryId(Long inventoryId);

  /**
   *
   * @param productId
   * @return
   */
  Inventory getInventoryByProductId(UUID productId);

 /**
   * @return List<Inventory>
   */

  List<Inventory> getAllInventory();

  /**
   * @param inventoryId
   * @return Inventory
   */
  Inventory updateInventory(Long inventoryId, Inventory inventory);

  void deleteInventory(Long inventoryId);

  void bulkDeleteInventoriesByInventoryId(List<Long> inventoryIds);


  void deductInventory(UUID productId, Integer quantity);

  Mono<Void> deductInventoryReactive(UUID productId, Integer quantity);



}
