/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of common-utils module of the Ecommerce Microservices project.
 */
package com.tjtechy;

import java.util.UUID;

/**
 * This is a Data Transfer Object (DTO) for deducting inventory.
 * It is used to transfer data between the client and server.
 * The DTO contains the product ID and the quantity to be deducted.
 */
public record DeductInventoryRequestDto(UUID productId, Integer quantity) {

}
