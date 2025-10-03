/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record CreateInventoryDto(
        @NotNull(message = "Product ID is required")
        UUID productId,
        @Positive(message = "Product available must be greater than 0")
        @NotNull(message = "Product available is required")
        Integer availableStock,
        @Positive(message = "Product reserve available must be greater than 0")
        @NotNull(message = "Product reserve available is required")
        Integer reservedQuantity, //by default, it is 1

        /**
         * //Todo: add the product expiry date to this record because it should
         * not be possible to add inventory for an expired product
         * It should not be possible to add inventory for an expired product.
         */
        @NotNull(message = "Expiry date is required")
        @FutureOrPresent(message = "Expiry date must be a future or present date")
        LocalDate expiryDate

) {


}
