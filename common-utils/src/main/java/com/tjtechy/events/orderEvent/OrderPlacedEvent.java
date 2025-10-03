/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.events.orderEvent;

public record OrderPlacedEvent(
        Long orderId,
        String customerEmail,
        String customDeviceToken,
        String customerPhoneNumber
) {
}
