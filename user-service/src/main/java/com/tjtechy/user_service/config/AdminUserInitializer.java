/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the User Service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.config;

import com.tjtechy.user_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import userutils.entity.User;

@Configuration
public class AdminUserInitializer {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties adminProperties;
  private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

  public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminProperties adminProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminProperties = adminProperties;
  }

  @Bean
  CommandLineRunner initializeAdminUser(){
    return args -> {
      userRepository.findByUserName(adminProperties.getUsername())
              .flatMap(existingUser ->{
                logger.info("=======Admin user already exists with username=====: {}", existingUser.getUserName());

                String configuredPassword = adminProperties.getPassword();

//                logger.info(
//                        "ADMIN_PASSWORD configured: {}, length: {}",
//                        configuredPassword != null && !configuredPassword.isBlank(),
//                        configuredPassword != null ? configuredPassword.length() : 0
//                );

                if(passwordEncoder.matches(configuredPassword, existingUser.getPassword())){
                  logger.info("Configured admin password already matches stored password");
                  return Mono.just(existingUser);
                }

                logger.info("Configured admin password changed. Updating stored admin password.");
                existingUser.setPassword(passwordEncoder.encode(configuredPassword));
                return userRepository.save(existingUser)
                        .doOnNext(updatedAdmin ->

                  logger.info("Admin password updated successfully for username: {}", updatedAdmin.getUserName())
                );


              })
              .switchIfEmpty(Mono.defer(() -> {
                User adminUser = new User();
                logger.info("====Admin ID before saving====: {}", adminUser.getUserId());
                adminUser.setUserName(adminProperties.getUsername());
                adminUser.setFirstName("AdminUser");
                adminUser.setLastName("Administrator");
                adminUser.setEmail("admin@admin.com");
                adminUser.setPhoneNumber("1234567890");
                adminUser.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
                adminUser.setRole(User.Role.ADMIN);
                adminUser.setEnabled(true);


                return userRepository.save(adminUser)
                        .doOnNext(savedAdmin ->

                          logger.info("Admin user created with username: {}", savedAdmin.getUserName())
                        );
              }))
              .doOnError(error -> logger.error("Error initializing admin user: {}", error.getMessage()))
              .subscribe();
    };
  }
}
