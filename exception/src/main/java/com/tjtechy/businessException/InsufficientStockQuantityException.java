/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of businessException package of the Ecommerce Microservices project.
 */
//package businessException;
package com.tjtechy.businessException;



import java.util.UUID;

/**
 * Exception thrown when there is insufficient stock quantity for a product.
 * It should be handled in the ExceptionHandlerAdvice class.
 */
public class InsufficientStockQuantityException extends RuntimeException {

    public InsufficientStockQuantityException(UUID id) {
        super("Insufficient stock quantity for product with id: " + id);
    }
}
