/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record UpdateOrderDto(
        @NotBlank(message = "Customer name is required")
                @Size(min = 2, message = "Customer name must be at least 2 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
                @Email(message = "Invalid email address")
        String customerEmail,

        /**
         * Value must not be {@code null} or empty.
         * The user phone number validation pattern:
         * ^: start String
         * (\\+\\d{1,3}[- ]?)?: Optional country code part
         * \\+\\d{1,3}: '+' followed by 1 to 3 digits (country code e.g +1, +44, +234)
         * [- ]?: Optional separator (either a hyphen or space)
         * \\d{7,15}: Main phone number part (7 to 15 digits)
         * $: end String
         * All these will be invalid:
         * 12345 (too short)
         * 1234567890123456 (too long)
         * +1-234-567-8901-2345 (because of multiple separators)
         */
        @NotBlank(message = "Customer phone is required")
                @Size(min = 7, max = 15, message = "Customer phone must be between 7 and 15 digits")
                @Pattern(
                        regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
                        message = "Invalid phone number format"
                )
        String customerPhone,

        @NotBlank(message = "Shipping address is required")
                @Size(min = 5, max = 255, message = "Shipping address must be at least 5 characters and at most 255 characters")
        String shippingAddress,

        @NotEmpty(message = "Order items cannot be empty")
        List<OrderItemDto> orderItems)
{
}
