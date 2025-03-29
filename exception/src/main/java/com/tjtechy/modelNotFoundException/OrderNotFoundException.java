/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of exception module of the Ecommerce Microservices project.
 */
package com.tjtechy.modelNotFoundException;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}
