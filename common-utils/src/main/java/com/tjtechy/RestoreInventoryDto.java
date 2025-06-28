/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy;

import java.util.UUID;

/**
 * This record is used to restore inventory for a given product by the specified quantity.
 * It is typically used when an order is cancelled, returned or updated and the inventory needs to be restored.
 *
 * @param productId the ID of the product
 * @param quantity  the quantity to restore to inventory
 */
public record RestoreInventoryDto(UUID productId, Integer quantity) {
}
