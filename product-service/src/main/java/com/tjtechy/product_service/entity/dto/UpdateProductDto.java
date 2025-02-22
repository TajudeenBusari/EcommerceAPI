/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.entity.dto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateProductDto(
        @NotEmpty(message = "Product Name is required")
        String productName,
        @NotEmpty(message = "Product Description is required")
        String productDescription,
        @NotEmpty(message = "Product Category is required")
        String productCategory,

        @NotNull(message = "Product price is required")
        @Positive(message = "Product price must be greater than 0")
        BigDecimal productPrice,

        @Positive(message = "Product Quantity must be greater than 0")
                @NotNull(message = "Product Quantity is required")
        Integer productQuantity,
        @Positive(message = "Available Stock must be greater than 0")
        @NotNull(message = "Available Stock is required")
        Integer availableStock

) {

}
