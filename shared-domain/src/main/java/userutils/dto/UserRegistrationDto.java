/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of shared-domain of the Ecommerce Microservices project.
 */

package userutils.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import userutils.entity.User;

public record UserRegistrationDto(
        @NotBlank(message = "Username is required")
    String userName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
    String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
    String password,

        @NotBlank(message = "First name is required")
    String firstName,

        @NotBlank(message = "Last name is required")
    String lastName,

        @Size(min = 7, max = 15, message = "Phone number must be between 1 and 15 characters")
        @Pattern(
                regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
                message = "Invalid phone number format"
        )
    String phoneNumber,

    User.Role role,

    boolean enabled

) {
}
