package com.tjtechy.notification_service.service.impl;

import com.tjtechy.events.orderEvent.*;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.SmsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SMSNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private SmsProvider smsProvider;

  @Spy
  @InjectMocks
  private SMSNotificationService smsNotificationService;

  private List<Notification> notifications;

  @BeforeEach
  void setUp() {
    notifications = new ArrayList<>(); //// Initialize to avoid NullPointerException
    var notification1 = new Notification(
            1L,
            101L,
            "Your order has been placed successfully",
            "customer1@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL
    );
    var notification2 = new Notification(
            2L,
            102L,
            "Your order has been cancelled",
            "customer2@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL);

    notifications.add(notification1);
    notifications.add(notification2);
  }

  @AfterEach
  void tearDown() {
  }

  /**
   * Test the processNotification method for successful notification sending.
   * We mock SmsProvider to simulate sending an SMS without actually calling an external SMS service.
   */
  @Test
  void processNotificationSuccess() throws Exception {
    //Given
    var notification = new Notification(
            1L,
            101L,
            "Your order has been placed successfully!",
            "+1234567890",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.SMS
    );
    var subject = "Order Confirmation";
    var messageBody = notification.getMessage();
    var to = notification.getRecipient();
    var orderId = notification.getOrderId();

    //mock
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
    doNothing().when(smsProvider).sendSms(to, messageBody);

    //When
    smsNotificationService.processNotification(to, subject, messageBody, orderId);

    //Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
    verify(smsProvider, times(1)).sendSms(to, messageBody);
    assertEquals(Notification.Status.SUCCESS, notification.getStatus());
  }

  /**
   * Test the processNotification method for failed notification sending.
   * We mock SmsProvider to simulate a failure when sending an SMS.
   */
  @Test
  void processNotificationFailure() throws Exception {
    //Given
    var notification = new Notification(
            1L,
            101L,
            "Your order has been placed successfully!",
            "+1234567890",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.SMS
    );
    var subject = "Order Confirmation";
    var messageBody = notification.getMessage();
    var to = notification.getRecipient();
    var orderId = notification.getOrderId();

    //mock
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
    doThrow(new RuntimeException("SMS provider error")).when(smsProvider).sendSms(to, messageBody);

    //When
    smsNotificationService.processNotification(to, subject, messageBody, orderId);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

    //Then
    verify(notificationRepository, times(2)).save(captor.capture());
    List<Notification> capturedNotifications = captor.getAllValues();
    var firstSave = capturedNotifications.get(0);
    var secondSave = capturedNotifications.get(1);
    verify(smsProvider, times(1)).sendSms(to, messageBody);
    assertEquals(Notification.Status.FAILED, firstSave.getStatus());
    assertEquals(Notification.Status.FAILED, secondSave.getStatus());
  }

  @Test
  void getChannel() {
  }

  @Test
  void listenToOrderPlacedSuccess() throws Exception {
    //Given
    var event = new OrderPlacedEvent(
            201L,
            "customer1@email.com",
            "dummyToken",
            "+1234567890",
            LocalDate.now(),
            ActionBy.ADMIN,
            Reason.ADMIN_ACTION
    );

    //MOCK
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(0));
    //When
    smsNotificationService.listenToOrderPlaced(event);
    //Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
    assertEquals(Notification.Status.SUCCESS, notifications.get(0).getStatus());

  }

  @Test
  void listenToOrderCancelledSuccess() {
    //Given
    var event = new OrderCancelledEvent(
            202L,
            notifications.get(1).getRecipient(),
            "dummyToken",
            "+0987654321",
            LocalDate.now(),
            ActionBy.ADMIN,
            Reason.ADMIN_ACTION
    );
    //MOCK
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(1));
    //When
    smsNotificationService.listenToOrderCancelled(event);
    //Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
    assertEquals(Notification.Status.SUCCESS, notifications.get(1).getStatus());
  }

  @Test
  void listenToOrderUpdatedSuccess() {
    //Given
    var event = new OrderUpdatedEvent(
            203L,
            notifications.get(0).getRecipient(),
            "dummyToken",
            "+1234567890",
            ActionBy.ADMIN,
            Reason.ADMIN_ACTION,
            LocalDate.now()
    );
    //MOCK
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(0));
    //When
    smsNotificationService.listenToOrderUpdated(event);
    //Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
    assertEquals(Notification.Status.SUCCESS, notifications.get(0).getStatus());

  }

  @Test
  void listenToOrderDeletedSuccess() {
    //Given
    var event = new OrderDeletedEvent(
            204L,
            notifications.get(1).getRecipient(),
            "dummyToken",
            "+0987654321",
            Reason.ADMIN_ACTION,
            ActionBy.ADMIN,
            LocalDate.now()
    );
    //MOCK
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(1));
    //When
    smsNotificationService.listenToOrderDeleted(event);
    //Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
    assertEquals(Notification.Status.SUCCESS, notifications.get(1).getStatus());
  }
}