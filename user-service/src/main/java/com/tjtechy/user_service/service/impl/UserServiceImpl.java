/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.service.impl;

import com.tjtechy.modelNotFoundException.UserNotFoundException;
import com.tjtechy.user_service.entity.User;
import com.tjtechy.user_service.repository.UserRepository;
import com.tjtechy.user_service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  ///PasswordEncoder bean is configured in the security module,
  /// without the bean, application will fail to start.
  private final PasswordEncoder passwordEncoder;
  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * @param user
   * @return
   */
  @Override
  public Mono<User> createUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    //TODO: Assign role based on registration data in future.
    // For example if admin is creating a user they should be able to set role to ADMIN.
    //For now, all new users are assigned CUSTOMER role.
    user.setRole(User.Role.CUSTOMER);
    user.setEnabled(true);
    return userRepository.save(user)
            //TODO: Consider changing this to doOnNext if issues arise, so that step is only called when user is actually saved.
            .doOnSuccess(savedUser -> {
              logger.info("Created new user with username: {}", savedUser.getUserName());
            })
            .doOnError(error -> {
              logger.error("Error creating user with username: {}: {}", user.getUserName(), error.getMessage());
            });
  }

  /**
   * @param username
   * @return
   */
  @Override
  @Cacheable(value = "user", key = "#username")
  public Mono<User> findUserByUsername(String username) {
    return userRepository.findByUserName(username)
            /**
             * .doOnNext is only called when a user is found, else, it is skipped.
             * if doOnSuccess is used, it will also log the Found with username even when user is not found,
             * because Mono.empty() is considered a successful completion.
             */
            .doOnNext(foundUser -> logger.info("Found with username: {}", username))
            .switchIfEmpty(Mono.defer(() -> {
              logger.warn("User not found with username: {}", username);
              return Mono.empty();
            }))
            .doOnError(error ->
                    logger.error("Error finding user with username: {}: {}", username, error.getMessage()));
  }

  /**
   * @param id
   * @return
   */
  @Override
  public Mono<User> findUserById(UUID id) {
    return null;
  }

  /**
   * @return
   */
  @Override
  public Flux<User> findAllUsers() {
    return null;
  }

  /**
   * @param id
   * @param user
   * @return
   */
  @Override
  public Mono<User> updateUser(UUID id, User user) {
    return null;
  }

  /**
   * @param id
   * @return
   */
  @Override
  public Mono<Void> deleteUser(UUID id) {
    //first find the user by id else throw error
    return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            //then delete the found user
            .flatMap(user -> {
              //if a user is enabled, first disable the user before deletion
              if (user.isEnabled()){
                user.setEnabled(false);
                return userRepository.save(user)
                        .doOnSuccess(u -> logger.info("Disabled user with id: {} before deletion", id))
                        .then(userRepository.delete(user))
                        .doOnSuccess(s -> logger.info("Deleted user with id : {}", id))
                        .doOnError(e -> logger.error("Error deleting user with id: {}: {}", id, e.getMessage()));
              } else {
                //directly delete the user if already disabled
                return userRepository.delete(user)
                        .doOnSuccess(su -> logger.info("Deleted user with id: {}", id))
                        .doOnError(err -> logger.error("Error occurred while deleting user with id: {}: {}", id, err.getMessage()));
              }
            });
  }

  /**
   * @param ids
   * @return
   */
  @Override
  public Mono<Void> deleteAllUsersByIds(Iterable<UUID> ids) {
    return null;
  }

  /**
   * @param users
   * @return
   */
  @Override
  public Flux<User> updateAllUsers(Flux<User> users) {
    return null;
  }
}
