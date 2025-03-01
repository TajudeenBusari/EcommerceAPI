/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.entity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductDto(

        UUID productId,
        String productName,
        String productCategory,
        String productDescription,
        Integer productQuantity,
        Integer availableStock,
        LocalDate expiryDate,
        BigDecimal productPrice

) {

}
