/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.service;

import com.tjtechy.Inventory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

  Inventory createInventory(Inventory inventory);


  Inventory getInventoryByInventoryId(Long inventoryId);


  Inventory getInventoryByProductId(UUID productId);


  List<Inventory> getAllInventory();

  Inventory updateInventory(Long inventoryId, Inventory inventory);

  void deleteInventory(Long inventoryId);

  void bulkDeleteInventoriesByInventoryId(List<Long> inventoryIds);


  void deductInventory(UUID productId, Integer quantity);

  Mono<Void> deductInventoryReactive(UUID productId, Integer quantity);

  void restoreInventoryStock(UUID productId, Integer quantity);



}
