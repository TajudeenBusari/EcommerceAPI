/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of modelNotFound package of the Ecommerce Microservices project.
 */
package modelNotFound;

import java.util.UUID;

/**
 * This class is a custom exception class that extends the RuntimeException class.
 * It is used to handle the exception when a product is not found in the database.
 * It should be handled in the ExceptionHandlerAdvice class.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {

        super("Product not found with id: " + id);
    }
}
