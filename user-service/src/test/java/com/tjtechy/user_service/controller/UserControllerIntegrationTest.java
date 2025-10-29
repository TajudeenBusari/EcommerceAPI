/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.user_service.entity.dto.UserRegistrationDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "eureka.client.fetchRegistry=false",
        "eureka.client.registerWithEureka=false",
        "spring.cloud.loadbalancer.enabled=false", // Disable load balancer
        "spring.cloud.service-registry.auto-registration.enabled=false",
        "redis.enabled=false", //disable redis
        "spring.cache.type=none", //disable caching
})
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use actual database configuration
@Tag("UserServiceIntegrationTest")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        // Exclude auto-configurations that are not needed for integration tests
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestSecurityConfig.class) //to disable security (csrf) for testing
public class UserControllerIntegrationTest {
  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Container
  private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("user_service_db")
          .withUsername("testuser")
          .withPassword("testpass");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
            postgreSQLContainer.getHost(),
//            postgreSQLContainer.getFirstMappedPort(),
            postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgreSQLContainer.getDatabaseName()
    ));
    registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
    registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
  }
  @BeforeAll
  static void startContainers() {
    postgreSQLContainer.start();
  }

  @AfterAll
  static void stopContainers() {
    if(postgreSQLContainer != null && postgreSQLContainer.isRunning()){
      postgreSQLContainer.stop();
    }

  }

 //private method to create user
  private Map<String, Object> createUser() throws Exception {
    var uniqueUsername = "BruceSmith" + System.currentTimeMillis();
    var userRegistrationDto = new UserRegistrationDto(
            uniqueUsername,
            "newuser" + System.currentTimeMillis() + "@email.com",
            "BruceSmith@123",
            "firstname1",
            "lastname1",
            "+1234567890",
            true
    );
    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    var responseBody = webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.userId").isNotEmpty()
            .jsonPath("$.data.userName").isEqualTo(uniqueUsername)
            .jsonPath("$.data.enabled").isEqualTo(true)
            .jsonPath("$.data.role").isEqualTo("CUSTOMER")
            .returnResult()
            .getResponseBody();

    var result = objectMapper.readValue(responseBody, Map.class);
    return (Map<String, Object>) result.get("data");
  }

  @Test
  @DisplayName("Check Add User (POST /user/register)")
  public void testCreateUserSuccess() throws Exception {
    Map<String, Object> createdUser = createUser();
    System.out.println("Created User: " + createdUser);
    //extract userId and username
    String userId = (String) createdUser.get("userId");
    String userName = (String) createdUser.get("userName");
    System.out.println("Created User ID: " + userId);
    System.out.println("Created Username: " + userName);
  }

  @Test
  @DisplayName("Check Add User Failure - Invalid Password Length (POST /user/register)")
  public void testCreateUserWithInvalidPasswordLength() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
            "BruceSmith" + System.currentTimeMillis(), //to ensure unique username
            "newuser@email.com",
            "Bruce", // Invalid password length
            "firstname1",
            "lastname1",
            "+1234567890",
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid password, not client error
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("A server internal error occurs");
  }

  @Test
  @DisplayName("Check Add User Failure - Invalid Email (POST /user/register)")
  public void testCreateUserWithInvalidEmail() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
            "BruceSmith" + System.currentTimeMillis(), //to ensure unique username
            "newuser@.com", // Invalid email format
            "BruceSmith@123", // Invalid password length
            "firstname1",
            "lastname1",
            "+1234567890",
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid password, not client error
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("A server internal error occurs");
  }


  /**
   * Test adding a new user successfully. Same as testCreateUserSuccess but without using helper method.
   * @throws Exception
   */
  @Test
  @DisplayName("Test Add User without helper method Success (POST /user/register)")
  public void testAddUserSuccess() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
            "BruceSmith" + System.currentTimeMillis(), //to ensure unique username
            //generate unique email as well
            "newuser" + System.currentTimeMillis() + "@email.com",
            "BruceSmith@123",
            "firstname1",
            "lastname1",
            "+1234567890",
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.userId").isNotEmpty()
            .jsonPath("$.data.userName").isEqualTo(userRegistrationDto.userName())
            .jsonPath("$.data.enabled").isEqualTo(true)
            .jsonPath("$.data.role").isEqualTo("CUSTOMER");
  }

  @Test
  @DisplayName("Check Get User By Username (GET /user/by-username?username=<username>)")
  public void testGetUserByUsernameSuccess() throws Exception {
    //first create user
    Map<String, Object> createdUser = createUser();
    String username = (String) createdUser.get("userName");

    //then get user by username
    webTestClient.get()
            .uri(baseUrl + "/user/by-username" +"?username=" + username)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.userName").isEqualTo(username);
  }

  @Test
  @DisplayName("Check Delete User By ID (DELETE /user/{userId})")
  public void testDeleteUserByIdSuccess() throws Exception {
    //first create user
    Map<String, Object> createdUser = createUser();
    String userId = (String) createdUser.get("userId");

    //then delete user by userId
    webTestClient.delete()
            .uri(baseUrl + "/user/" + userId)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.code").isEqualTo(200)
            .jsonPath("$.message").isEqualTo("User deleted successfully");
  }
}
