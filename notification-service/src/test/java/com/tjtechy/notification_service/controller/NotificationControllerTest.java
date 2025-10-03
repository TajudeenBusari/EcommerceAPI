package com.tjtechy.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.exception.ExceptionHandlingAdvice;
import com.tjtechy.notification_service.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc
@ContextConfiguration(classes = NotificationController.class)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none", // Disable caching for tests
        "spring.cloud.config.enabled=false", // Disable Spring Cloud Config for tests
        "spring.redis.enabled=false" // Disable Redis for tests
})
@Import(ExceptionHandlingAdvice.class)
class NotificationControllerTest {
  @MockitoBean
  private NotificationService notificationService;

  @Autowired
  private MockMvc mockMvc;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  private List<Notification> notificationList;

  ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    notificationList = new ArrayList<>();//initialize the list to empty list avoid NullPointerException
    var notification1 = new Notification(
            1L,
            101L,
            "Your order has been placed successfully.",
            "customer1@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL
    );
    var notification2 = new Notification(
            2L,
            102L,
            "Your order has been CANCELLED.",
            "customer2@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL
    );
    //old notifications for cleanup testing
    var notification3 = new Notification(
            3L,
            103L,
            "Your order has been DELIVERED.",
            "",
            Notification.Status.SUCCESS,
            LocalDate.now().minusDays(40),
            Notification.Channel.EMAIL
    );

    notificationList.add(notification1);
    notificationList.add(notification2);
    notificationList.add(notification3);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void getAllNotificationsSuccess() throws Exception {
    //Given
    when(notificationService.getAllNotifications()).thenReturn(notificationList);

    //When and //Then
    mockMvc.perform(get(baseUrl + "/notification"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notifications fetched successfully"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].notificationId").value(1))
            .andExpect(jsonPath("$.data[0].orderId").value(101))
            .andExpect(jsonPath("$.data[0].message").value("Your order has been placed successfully."))
            .andExpect(jsonPath("$.data[0].recipient").value("customer1@email.com"))
            .andExpect(jsonPath("$.data[0].status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].channel").value("EMAIL"))
            .andExpect(jsonPath("$.data[1].notificationId").value(2))
            .andExpect(jsonPath("$.data[1].orderId").value(102))
            .andExpect(jsonPath("$.data[1].message").value("Your order has been CANCELLED."))
            .andExpect(jsonPath("$.data[1].recipient").value("customer2@email.com"))
            .andExpect(jsonPath("$.data[1].status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[1].channel").value("EMAIL"))
            .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void getNotificationByIdSuccess() throws Exception {
    //Given
    when(notificationService.getNotificationById(notificationList.get(0).getNotificationId()))
            .thenReturn(notificationList.get(0));

    //When and //Then
    mockMvc.perform(get(baseUrl + "/notification/{notificationId}", notificationList.get(0).getNotificationId()))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notification fetched successfully"))
            .andExpect(jsonPath("$.data.notificationId").value(1))
            .andExpect(jsonPath("$.data.orderId").value(101))
            .andExpect(jsonPath("$.data.message").value("Your order has been placed successfully."));
  }

  @Test
  void removeNotificationByIdSuccess() throws Exception {
    //Given
    doNothing().when(notificationService).removeNotification(notificationList.get(0).getNotificationId());
    //When and //Then
    mockMvc.perform(delete(baseUrl + "/notification/{notificationId}", notificationList.get(0).getNotificationId()))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notification removed successfully"))
            .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void removeAllNotificationsSuccess() throws Exception {
    //Given
    //return true when notifications are cleaned up from the list
    when(notificationService.getAllNotifications()).thenReturn(notificationList);
    when(notificationService.bulkRemoveNotificationsBySentAtLessThanEqual()).thenReturn(true);

    //When and Then
    mockMvc.perform(delete(baseUrl + "/notification/cleanup"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notifications of 30 days or older successfully deleted"))
            .andExpect(jsonPath("$.code").value(200));
  }

  @Test
  void testBulkRemoveNotifications() throws Exception {
    //given
    var id1 = notificationList.get(0).getNotificationId();
    var id2 = notificationList.get(1).getNotificationId();
    var ids = List.of(id1, id2);

    doNothing().when(notificationService).bulkRemoveNotification(ids);
    //when and then
    mockMvc.perform(delete(baseUrl + "/notification/bulk-delete")
            .contentType("application/json")
            .content(mapper.writeValueAsString(ids)))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Notifications removed successfully"))
            .andExpect(jsonPath("$.code").value(200));
    verify(notificationService, times(1)).bulkRemoveNotification(ids);
  }

}