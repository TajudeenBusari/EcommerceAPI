/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.repository;


import com.tjtechy.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(UUID productId);

    Inventory findByProductIdAndReservedQuantity(UUID productId, Integer reservedQuantity);

    Inventory findByProductIdAndAvailableStock(UUID productId, Integer availableStock);


}
