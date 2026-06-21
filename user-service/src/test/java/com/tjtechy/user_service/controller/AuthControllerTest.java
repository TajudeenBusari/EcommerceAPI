package com.tjtechy.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.RedisCacheConfig;
import com.tjtechy.user_service.service.impl.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import userutils.dto.LoginRequestDto;
import userutils.dto.LoginResponseDto;
import userutils.dto.UserDto;
import userutils.entity.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AuthController.class)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none", // Disable caching for tests
        "spring.redis.enabled=false", // Disable Redis for tests
        "eureka.client.enabled=false",// Disable Eureka client for tests
        "spring.cloud.config.enabled=false" // Disable Spring Cloud Config for tests
})
@ImportAutoConfiguration(exclude = {
        //exclude auto configurations that are not needed for the test
        RedisCacheConfig.class, EurekaClientAutoConfiguration.class
})
@AutoConfigureWebTestClient
@Import(TestSecurityConfig.class) //to disable security (csrf) for testing
class AuthControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private RedisConnectionFactory redisConnectionFactory;

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void getLoginInfoSuccess() throws Exception {

    //given
    var loginRequest = new LoginRequestDto("testuser", "testpassword");
    var userDto = new UserDto(
            UUID.randomUUID(), "testUsername", "testEmail", "testPassword", "testFirstName", true,  User.Role.CUSTOMER
    );

    var json = objectMapper.writeValueAsString(loginRequest);
    when(authService.createLoginInfo(any(String.class), any(String.class)))
            .thenReturn(Mono.just(new LoginResponseDto(userDto, "testToken")));

    //when and then
    var result = webTestClient.post()
            .uri(baseUrl + "/auth/login")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("User Info and JSON Web Token")
            .jsonPath("$.data.userInfo.userName").isEqualTo("testUsername")
            .jsonPath("$.data.token").isEqualTo("testToken");
    //print the response body for debugging
    var responseBody = result.returnResult().getResponseBody();
    String responseString = new String(responseBody);
    System.out.println("Response Body: " + responseString);
  }

  @Test
  void getLoginInfoFailure() throws Exception {
    //given
    var loginRequest = new LoginRequestDto("invalidUser", "invalidPassword");
    var json = objectMapper.writeValueAsString(loginRequest);
    when(authService.createLoginInfo(any(String.class), any(String.class)))
            .thenReturn(Mono.error(new UsernameNotFoundException("username not found with username: invalidUser")));

    //when and then
    var result = webTestClient.post()
            .uri(baseUrl + "/auth/login")
            .bodyValue(json)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.flag").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("username or password is incorrect");
    var responseBody = result.returnResult().getResponseBody();
    String responseString = new String(responseBody);
    System.out.println("Response Body: " + responseString);
  }
}