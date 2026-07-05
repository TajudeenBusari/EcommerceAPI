/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.service.impl;

import com.tjtechy.security.config.JwtProvider;
import com.tjtechy.user_service.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import userutils.dto.LoginResponseDto;
import userutils.entity.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private UserService userService;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  private List<User> users;



  @BeforeEach
  void setUp() {
    users = new ArrayList<>();
    var user1 = new User();
    user1.setUserId(UUID.randomUUID());
    user1.setUserName("john_doe");
    user1.setPassword("encodedPassword123");
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setEmail("john@doe.com");
    user1.setPhoneNumber("1234567890");
    user1.setRole(User.Role.CUSTOMER);
    user1.setEnabled(true);
    user1.setCreatedAt(LocalDate.now());
    user1.setUpdatedAt(LocalDate.now());
    users.add(user1);

    var user2 = new User();
    user2.setUserId(UUID.randomUUID());
    user2.setUserName("jane_smith");
    user2.setPassword("encodedPassword456");
    user2.setFirstName("Jane");
    user2.setLastName("Smith");
    user2.setEmail("jane@smith.com");
    user2.setPhoneNumber("0987654321");
    user2.setRole(User.Role.CUSTOMER);
    user2.setEnabled(true);
    user2.setCreatedAt(LocalDate.now());
    user2.setUpdatedAt(LocalDate.now());
    users.add(user2);

    //USER WITH INVALID PASSWORD LENGTH
    var user3 = new User();
    user3.setUserId(UUID.randomUUID());
    user3.setUserName("invalid_user");
    user3.setPassword("123"); // Invalid password length
    user3.setFirstName("Invalid");
    user3.setLastName("User");
    user3.setEmail("invalid@invalid.com");
    user3.setPhoneNumber("1112223333");
    user3.setRole(User.Role.CUSTOMER);
    user3.setEnabled(true);
    user3.setCreatedAt(LocalDate.now());
    user3.setUpdatedAt(LocalDate.now());
    users.add(user3);


  }

  @AfterEach
  void tearDown() {
  }

  /**
   * User exists
   * Password is correct
   * Expected: A map containing a JWT token and user information is returned.
   * This test verifies that when a valid username and password are provided,
   * the createLoginInfo method returns a map containing a JWT token and user information.
   */
  @Test
  void createLoginInfoSuccess() {

    //given
    given(userService.findUserByUsername("john_doe")).willReturn(Mono.just(users.getFirst()));
    given(passwordEncoder.matches("password123", "encodedPassword123")).willReturn(true);
    given(jwtProvider.createToken(any(Authentication.class))).willReturn("mockedJwtToken");

    //when
    Mono<LoginResponseDto> result = authService.createLoginInfo("john_doe", "password123");

    //then
    StepVerifier.create(result)
            .assertNext(res -> {
              assertEquals("mockedJwtToken", res.token());
              assertNotNull(res.userInfo());
            }).verifyComplete();
  }

  /**
   * User does not exist
   * Expected: An error with message "User not found with username: non_existent_user" or UsernameNotFoundException is thrown.
   * This test verifies that when a non-existent username is provided,
   * the createLoginInfo method returns an error with the message "User not found with username: non_existent_user".
   */
  @Test
  void createLoginInfoUserNotFound() {
    //given
    given(userService.findUserByUsername("non_existent_user")).willReturn(Mono.empty());

    //when
    Mono<LoginResponseDto> result = authService.createLoginInfo("non_existent_user", "password123");

    //then
    StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("User not found with username: non_existent_user"))
            .verify();
  }

  /**
   * User exists
   * Password is incorrect
   * Expected: An error with message "Invalid username or password" or BadCredentialsException is thrown.
   * This test verifies that when an incorrect password is provided,
   * the createLoginInfo method returns an error with the message "Invalid username or password".
   */
  @Test
  void createLoginInfoInvalidPassword(){

    //given
    given(userService.findUserByUsername("john_doe")).willReturn(Mono.just(users.getFirst()));
    given(passwordEncoder.matches("wrongPassword", "encodedPassword123")).willReturn(false);

    //when
    Mono<LoginResponseDto> result = authService.createLoginInfo("john_doe", "wrongPassword");

    //then
    StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                    throwable.getMessage().equals("Invalid username or password"))
            .verify();
  }
}