package com.tjtechy;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

//TODO: Move this to common-utils module
public record UpdateInventoryDto(

        @NotNull(message = "Product ID is required")
        UUID productId,
        @Positive(message = "Product available must be greater than 0")
        @NotNull(message = "Product available is required")
        Integer availableStock,
        @Positive(message = "Product available must be greater than 0")
        @NotNull(message = "Product available is required")
        Integer reservedQuantity

) {
}
