/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the common-utils module of the Ecommerce Microservices project.
 */

package com.tjtechy.events.orderEvent;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

public record OrderUpdatedEvent(
        Long orderId,
        String customerEmail,
        String customDeviceToken,
        String customerPhoneNumber,
        @Enumerated(EnumType.STRING)
        ActionBy actionBy,
        @Enumerated(EnumType.STRING)
        Reason reason,
        LocalDate updatedDate
) {

}
