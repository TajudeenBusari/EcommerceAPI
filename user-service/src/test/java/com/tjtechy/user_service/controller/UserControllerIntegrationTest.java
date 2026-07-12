/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service test module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tjtechy.Result;

import org.junit.jupiter.api.*;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.config.DiscoveryClientOptionalArgsConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;
import userutils.dto.LoginRequestDto;

import userutils.dto.UserRegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
//import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import userutils.entity.User;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
        "spring.redis.enabled=false", //disable redis
        "spring.cache.type=none" //disable caching
        //"spring.cloud.compatibility-verifier.enabled=false"
})
@AutoConfigureWebTestClient
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use actual database configuration
@Tag("UserServiceIntegrationTest")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        // Exclude auto-configurations that are not needed for integration tests
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class,
        SimpleDiscoveryClientAutoConfiguration.class,
        DiscoveryClientOptionalArgsConfiguration.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestConfig.class) //TO INITIALIZE THE ADMIN USER IN THE DB for CI TO PASS
public class UserControllerIntegrationTest {
  @Autowired
  private WebTestClient webTestClient;


  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @BeforeAll
  static void startContainers() {

    postgreSQLContainer.start();
  }

  @AfterAll
  static void stopContainers() {
    postgreSQLContainer.stop();
  }


  @Container
  private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
          .withDatabaseName("user_service_db")
          .withUsername("testuser")
          .withPassword("testpass");

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
            postgreSQLContainer.getHost(),
            postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgreSQLContainer.getDatabaseName()
    ));
    registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
    registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
  }

  //private method to login and generate token
  //the application.test.yml with the db initializer creates the admin user (role admin) with username and password as "admin",
  // so we can use that to login and get the token for authenticated requests if needed in the future tests.
  private String loginInfo() throws Exception {
    var loginRequest = new LoginRequestDto("admin", "Admin@123");
    var json = objectMapper.writeValueAsString(loginRequest);
    var response = webTestClient.post()
            .uri(baseUrl+ "/auth/login")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody(Result.class)
            .returnResult()
            .getResponseBody();
    assert response != null;
    assertThat(response.getData()).isInstanceOf(Map.class);

    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) response.getData();
    return (String) data.get("token");
  }
  //private method to create user
  private Map<String, Object> createUser() throws Exception {
    String token = loginInfo();
    //System.out.println("=============Generated token===========: " + token);
    var uniqueUsername = "BruceSmith" + System.currentTimeMillis();
    var userRegistrationDto = new UserRegistrationDto(
            uniqueUsername,
            "newuser" + System.currentTimeMillis() + "@email.com",
            "BruceSmith@123",
            "firstname1",
            "lastname1",
            "+1234567890",
            User.Role.CUSTOMER, // Set role to CUSTOMER for testing
            true
    );
    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    var responseBody = webTestClient.post()
            .uri(baseUrl + "/user/register")
            .header("Authorization", "Bearer " + loginInfo()) // Include the token in the Authorization header
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.userId").isNotEmpty()
            .jsonPath("$.data.userName").isEqualTo(uniqueUsername)
            .jsonPath("$.data.enabled").isEqualTo(true)
            .jsonPath("$.data.role").isEqualTo("CUSTOMER")
            .returnResult()
            .getResponseBody();

    assert responseBody != null;
    var result = objectMapper.readValue(responseBody, Map.class);
    assert result != null;
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) result.get("data");
    return data;
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
            User.Role.CUSTOMER, // Set role to CUSTOMER for testing
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + loginInfo()) // Include the token in the Authorization header
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid password, not client error
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
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
            User.Role.CUSTOMER, // Set role to CUSTOMER for testing
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + loginInfo()) // Include the token in the Authorization header
            .exchange()
            .expectStatus().is5xxServerError() // Expecting server error due to invalid password, not client error
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("A server internal error occurs");
  }


  /**
   * Test adding a new user successfully. Same as testCreateUserSuccess but without using the helper method
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
            User.Role.CUSTOMER, // Set role to CUSTOMER for testing
            false
    );

    var requestBody = objectMapper.writeValueAsString(userRegistrationDto);
    webTestClient.post()
            .uri(baseUrl + "/user/register")
            .bodyValue(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + loginInfo())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
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
            .header("Authorization", "Bearer " + loginInfo())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
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
            .header("Authorization", "Bearer " + loginInfo())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
              System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.code").isEqualTo(200)
            .jsonPath("$.message").isEqualTo("User deleted successfully");
  }

  @Test
  @DisplayName("Check Login User (POST /auth/login)")
  public void testLoginUserSuccess() throws Exception {

    //first create user
    Map<String, Object> createdUser = createUser();
    String username = (String) createdUser.get("userName");
    String password = "BruceSmith@123"; //same as the password used in createUser()

    //then login with the created user
    var loginRequestBody = objectMapper.writeValueAsString(Map.of(
            "username", username,
            "password", password
    ));

    webTestClient.post()
            .uri(baseUrl + "/auth/login")
            .bodyValue(loginRequestBody)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(res -> {
              assert res.getResponseBody() != null;
              String body = new String(res.getResponseBody());
              ObjectMapper mapper = new ObjectMapper();
              try {
                JsonNode jsonNode = mapper.readTree(body);
                ((ObjectNode) jsonNode.path("data")).put("token", "***"); // Mask the token value in the logs
                System.out.println("ResponseBody: " + mapper.writeValueAsString(jsonNode));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              //System.out.println("ResponseBody: " + new String(res.getResponseBody()));
            })
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.userInfo.userName").isEqualTo(username)
            .jsonPath("$.data.token").isNotEmpty();
  }
}
