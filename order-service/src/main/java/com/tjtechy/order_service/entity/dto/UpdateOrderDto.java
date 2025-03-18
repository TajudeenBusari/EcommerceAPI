/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateOrderDto(
        @NotBlank(message = "Customer name is required")
                @Size(min = 2, message = "Customer name must be at least 2 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
                @Email(message = "Invalid email address")
        String customerEmail,

        @NotBlank(message = "Shipping address is required")
                @Size(min = 5, max = 255, message = "Shipping address must be at least 5 characters and at most 255 characters")
        String shippingAddress,

        @NotEmpty(message = "Order items cannot be empty")
        List<OrderItemDto> orderItems)
{
}
