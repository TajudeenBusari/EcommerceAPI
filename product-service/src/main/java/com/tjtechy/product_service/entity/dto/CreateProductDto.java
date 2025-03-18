/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.entity.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProductDto(
        @NotEmpty(message = "Product name is required")
        String productName,

        @NotEmpty(message = "Product name is required")
        String productDescription,

        @NotEmpty(message = "Product name is required")
        String productCategory,

        @Positive(message = "Product quantity must be greater than 0")
                @NotNull(message = "Product quantity is required")
        Integer productQuantity,

        @Positive(message = "Product available must be greater than 0")
                @NotNull(message = "Product available is required")
        Integer availableStock,

        @NotNull(message = "Product price is required")
        @Positive(message = "Product price must be greater than 0")
        BigDecimal productPrice,

        @NotNull(message = "Manufactured date is required")
                @PastOrPresent(message = "Manufactured date must be a past or present date")
        LocalDate manufacturedDate,
        @NotNull(message = "Expiry date is required")
                @FutureOrPresent(message = "Expiry date must be a future or present date")
        LocalDate expiryDate
) {
}
/**
 * sample request body to add a product
 {
 "productName": "product1",
 "productDescription": "product1 description",
 "productCategory": "product1 category",
 "productQuantity": 10,
 "availableStock": 10,
 "productPrice": 999.99,
 "manufacturedDate": "2025-02-20",
 "expiryDate": "2027-02-20"
 }
 */
