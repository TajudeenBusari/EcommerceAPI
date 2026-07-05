/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.service;
import userutils.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.UUID;

public interface UserService {
  Mono<User> createUser(User user);
  Mono<User> findUserByUsername(String username);
  Mono<User> findUserById(UUID id);
  /**
   * This is not ideal for large datasets as it loads all users into memory.
   * It means wait till all users are fetched, then emit them as one big list.
   * A better approach is to use Flux<User> which streams users one by one.
   */
  //Mono<List<User>> getAllUsers();
  Flux<User> findAllUsers();
  Mono<User> updateUser(UUID id, User user);
  Mono<Void> deleteUser(UUID id);
  Mono<Void> deleteAllUsersByIds(Iterable<UUID> ids);
  Flux<User> updateAllUsers(Flux<User> users);
}
