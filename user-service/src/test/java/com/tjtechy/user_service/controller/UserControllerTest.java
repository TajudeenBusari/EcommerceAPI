/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.RedisCacheConfig;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import userutils.entity.User;
import userutils.dto.UserRegistrationDto;
import com.tjtechy.user_service.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = UserController.class)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none", // Disable caching for tests
        "spring.redis.enabled=false", // Disable Redis for tests
        "eureka.client.enabled=false",// Disable Eureka client for tests
        "spring.cloud.config.enabled=false" // Disable Spring Cloud Config for tests
})
@ImportAutoConfiguration(exclude = {
        //exclude auto configurations that are not needed for the test
        RedisCacheConfig.class,
        EurekaClientAutoConfiguration.class,

})
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class) //to disable security (csrf) for testing
class UserControllerTest {
  /**
   * This is used instead of MockMvc for testing WebFlux controllers.
   */
  @Autowired
  private WebTestClient webTestClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @MockitoBean
  private UserService userService;

  /** MockitoBean creates a mock of the RedisConnectionFactory
   * to prevent actual Redis connections during testing.
   */
  @MockitoBean
  private RedisConnectionFactory redisConnectionFactory;

  private List<User> users;


  @BeforeEach
  void setUp() {
    users = new ArrayList<>();
    var user1 = new User();
    user1.setUserId(UUID.randomUUID());
    user1.setUserName("john_doe");
    user1.setPassword("password123");
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
    user2.setPassword("password456");
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

    //USER WITH INVALID EMAIL FORMAT
    var user4 = new User();
    user4.setUserId(UUID.randomUUID());
    user4.setUserName("bad_email_user");
    user4.setPassword("validPassword1");
    user4.setFirstName("BadEmail");
    user4.setLastName("User");
    user4.setEmail("bademail.com"); // Invalid email format
    user4.setPhoneNumber("4445556666");
    user4.setRole(User.Role.CUSTOMER);
    user4.setEnabled(true);
    user4.setCreatedAt(LocalDate.now());
    user4.setUpdatedAt(LocalDate.now());
    users.add(user4);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  @DisplayName("Add User Success POST api/v1/user/register")
  void addUserSuccess() throws Exception {
    //given
    var registrationRequestDto = new UserRegistrationDto(
            "john_doe",
            "john@doe.com",
            "securePassword",
            "John",
            "Doe",
            "1234567890",
            User.Role.CUSTOMER,
            true
    );
    var json = objectMapper.writeValueAsString(registrationRequestDto);
    when(userService.createUser(any(User.class))).thenReturn(Mono.just(users.getFirst()));

    //when and then
    webTestClient.post()

            .uri(baseUrl + "/user/register")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("User created successfully")
            .jsonPath("$.data.userName").isEqualTo("john_doe")
            .jsonPath("$.data.email").isEqualTo("john@doe.com");
  }

  @Test
  @DisplayName("Add User Failure - Invalid Password Length POST api/v1/user/register")
  void addUserWithInvalidPassword() throws Exception {
    //given
    var registrationRequestDto = new UserRegistrationDto(
            "invalid_user",
            "invalid@invalid.com",
            "123", // Invalid password length
            "Invalid",
            "User",
            "1112223333",
            User.Role.CUSTOMER,
            true
    );
    var json = objectMapper.writeValueAsString(registrationRequestDto);
    when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Error creating user with username: invalid_user: Password must be at least 6 characters long"));
    //when and then
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid password, not client error
            .expectBody()
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("A server internal error occurs");
  }

  @Test
  @DisplayName("Add User Failure - Invalid Email Format POST api/v1/user/register")
  void addUserWithInvalidEmail() throws Exception {
    //given
    var registrationRequestDto = new UserRegistrationDto(
            "bad_email_user",
            "bademail.com", // Invalid email format
            "validPassword1",
            "BadEmail",
            "User",
            "4445556666",
            User.Role.CUSTOMER,
            true
    );
    var json = objectMapper.writeValueAsString(registrationRequestDto);
    when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Error creating user with username: bad_email_user: Email format is invalid"));
    //when and then
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid email, not client error
            .expectBody()
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("A server internal error occurs");
  }

  @Test
  @DisplayName("Get User By Username Success GET api/v1/user/by-username")
  void getUserByUsernameSuccess() {
    //given
    String username = "john_doe";
    when(userService.findUserByUsername(username)).thenReturn(Mono.just(users.getFirst()));
    //when and then
    webTestClient.get()
            .uri(baseUrl + "/user/by-username" + "?username=" + username)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("User retrieved successfully")
            .jsonPath("$.data.userName").isEqualTo("john_doe");
  }

  @Test
  @DisplayName("Delete User Success DELETE api/v1/user/{userId}")
  void deleteUserSuccess() {
    //given
    UUID userId = users.getFirst().getUserId();
    when(userService.deleteUser(userId)).thenReturn(Mono.empty());
    //when and then
    webTestClient.delete()
            .uri(baseUrl + "/user/" + userId)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("User deleted successfully");
  }
}