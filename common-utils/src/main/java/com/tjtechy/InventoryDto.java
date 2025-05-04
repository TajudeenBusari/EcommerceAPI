/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy;

import java.util.UUID;

/**
 * InventoryDto: Returned by the inventory service
 * @param inventoryId
 * @param productId
 * @param reservedQuantity
 */
public record InventoryDto(
        Long inventoryId,
        UUID productId,
        Integer reservedQuantity

) {

}
