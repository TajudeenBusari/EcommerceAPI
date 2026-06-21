/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */
package userutils.dto;


import userutils.entity.User;

import java.util.UUID;

public record UserDto(
        UUID userId,
    String userName,
    String email,
    String firstName,
    String lastName,
    boolean enabled,
    User.Role role
) {
}
