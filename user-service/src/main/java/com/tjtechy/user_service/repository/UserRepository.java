/*
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.repository;


import userutils.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;


import java.util.UUID;

/**
 * This R2DBC repository internally extends ReactiveCrudRepository, which provides reactive CRUD operations
 * for the User entity. It allows non-blocking database interactions using reactive programming paradigms.
 */
public interface UserRepository extends R2dbcRepository<User, UUID> {
  // Custom query method to find a user by username
  Mono<User> findByUserName(String userName);
}
