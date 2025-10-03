/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.notification_service.config.FirebaseConfig;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.impl.TwilioSmsProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {

                "spring.cloud.config.enabled=false",
                "spring.cloud.config.discovery.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.datasource.url=jdbc:tc:postgresql:15.0:///notificationdb",
                "eureka.client.fetchRegistry=false",
                "eureka.client.registerWithEureka=false",
                "spring.cloud.loadbalancer.enabled=false", // Disable load balancer
                "spring.cloud.service-registry.auto-registration.enabled=false",
                "redis.enabled=false", //disable redis
                "spring.cache.type=none", //disable caching
        })
@Tag("NotificationServiceControllerIntegrationTest")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
        EurekaClientAutoConfiguration.class,
        EurekaDiscoveryClientConfiguration.class
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationControllerIntegrationTest {

  /**
   * Mock these to prevent context load issues related to external services.
   */
  @MockitoBean
  private TwilioSmsProvider twilioSmsProvider;
  @MockitoBean
  private FirebaseConfig firebaseConfig;

  @Autowired
  MockMvc mockMvc;

  @Autowired
  NotificationRepository notificationRepository;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  private final ObjectMapper mapper = new ObjectMapper();

  @Container
  private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =  new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("notificationdb")
          .withUsername("test")
          .withPassword("test");

  @Container
  private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

  //Register dynamic properties for PostgreSQL and Kafka
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {

    //PostgreSQL properties
    registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRE_SQL_CONTAINER::getDriverClassName);

    //Kafka properties
    registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
  }

  @BeforeAll
  static void startContainers() {
    POSTGRE_SQL_CONTAINER.start();
    KAFKA_CONTAINER.start();
  }

  @AfterAll
  static void stopContainers() {
    if (POSTGRE_SQL_CONTAINER != null && POSTGRE_SQL_CONTAINER.isRunning()) {
      POSTGRE_SQL_CONTAINER.stop();
    }
    if (KAFKA_CONTAINER != null && KAFKA_CONTAINER.isRunning()) {
      KAFKA_CONTAINER.stop();
    }
  }

  //Save some notifications to the database before each test
  private List<Notification> notificationList;

  @BeforeEach
  void setUp() {
    notificationList = new ArrayList<>(); //initialize the list to empty list avoid NullPointerException
    var notification1 = new Notification(
            null,
            101L,
            "Your order has been placed successfully.",
            "customer1@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL
    );
    var notification2 = new Notification(
            null,
            102L,
            "Your order has been CANCELLED.",
            "customer2@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.SMS);

    var notification3 = new Notification(
            null,
            103L,
            "Your order has been DELIVERED.",
            "customer3@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now().minusDays(40),
            Notification.Channel.PUSH_NOTIFICATION
    );
    //notification older than 30 days
    var notification4 = new Notification(
            null,
            104L,
            "Your order is being PREPARED.",
            "csutomer4@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now().minusDays(31),
            Notification.Channel.EMAIL);

    notificationList.add(notification1);
    notificationList.add(notification2);
    notificationList.add(notification3);
    notificationList.add(notification4);
    notificationRepository.saveAll(notificationList);
  }

  @Test
  @DisplayName("Check Get all Notifications Success (GET /api/v1/notification)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testGetNotificationsSuccess() throws Exception {
    var result = mockMvc.perform(get(baseUrl + "/notification"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notifications fetched successfully"))
            .andExpect(jsonPath("$.data").isArray());
    //Additional assertions can be added here if needed
    var response = result.andReturn().getResponse();
    System.out.println("Response: " + response.getContentAsString());
  }


  @Test
  @DisplayName("Check Get Notification by ID Success (GET /api/v1/notification/{notificationId})")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testGetNotificationByIdSuccess() throws Exception {
    var notification = notificationList.get(0); // Get the first notification
    var result = mockMvc.perform(get(baseUrl + "/notification/{notificationId}", notification.getNotificationId()))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notification fetched successfully"))
            .andExpect(jsonPath("$.data.notificationId").value(notification.getNotificationId()))
            .andExpect(jsonPath("$.data.orderId").value(notification.getOrderId()))
            .andExpect(jsonPath("$.data.message").value(notification.getMessage()))
            .andExpect(jsonPath("$.data.recipient").value(notification.getRecipient()))
            .andExpect(jsonPath("$.data.status").value(notification.getStatus().toString()))
            .andExpect(jsonPath("$.data.channel").value(notification.getChannel().toString()));
    //Additional assertions can be added here if needed
    var response = result.andReturn().getResponse();
    System.out.println("Response: " + response.getContentAsString());
  }

  @Test
  @DisplayName("Check Delete Notification by ID Success (DELETE /api/v1/notification/{notificationId})")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testRemoveNotificationByIdSuccess() throws Exception {
    var notification = notificationList.get(0); // Get the first notification
    var result = mockMvc.perform(delete(baseUrl + "/notification/{notificationId}", notification.getNotificationId()))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notification removed successfully"));
    //Additional assertions can be added here if needed
    var response = result.andReturn().getResponse();
    System.out.println("Response: " + response.getContentAsString());
  }

  @Test
  @DisplayName("Check Cleanup Old Notifications Success (DELETE /api/v1/notification/cleanup)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testCleanupOldNotificationsSuccess() throws Exception {
    var result = mockMvc.perform(delete(baseUrl + "/notification/cleanup"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notifications of 30 days or older successfully deleted"));
    //Additional assertions can be added here if needed
    var response = result.andReturn().getResponse();
    System.out.println("Response: " + response.getContentAsString());
  }
}
