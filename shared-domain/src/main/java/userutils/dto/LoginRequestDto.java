/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the shared-domain of the Ecommerce Microservices project.
 */
package userutils.dto;

public record LoginRequestDto(
        String username,
        String password
) {
}
