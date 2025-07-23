/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.businessException;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(Long orderId) {
        super("Order with ID " + orderId + " has already been cancelled.");
    }

    public OrderAlreadyCancelledException(Long orderId, Throwable cause) {
        super("Order with ID " + orderId + " has already been cancelled.", cause);
    }
}
