package com.tjtechy.notification_service.service.impl;

import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
import com.tjtechy.notification_service.config.MailProperties;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private MailProperties mailProperties;

  @Mock
  private MimeMessage mimeMessage;

  /**
   * When you annotate a class with @InjectMocks, Mockito will create a real instance of the class
   * and inject the mocks (annotated with @Mock) into it. So, emailNotificationService will be a real instance
   * and not a mock. This means that its methods will execute their actual code unless they are specifically stubbed.
   * sendEmail is a real method in EmailNotificationService, and Mockito can only stub
   * mocked or spied methods. Since emailNotificationService is not a mock but a real instance.
   * A Spy wraps a real object but allows you to stub/override selected methods while letting others
   * behave normally. In this case, by annotating emailNotificationService with @Spy, it is now a spy.
   * Now, processNotification will still run the real logic (like saving to notificationRepository),
   * but when it calls sendEmail(...), Mockito intercepts it and does nothing instead of trying to send a real email.
   * */
  @Spy
  @InjectMocks
  private EmailNotificationService emailNotificationService;

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
            Notification.Status.FAILED,
            LocalDate.now(),
            Notification.Channel.EMAIL);

    notifications.add(notification1);
    notifications.add(notification2);
  }


  /**
   * Tests the processNotification method for successful email sending.
   * Verifies that the notification status is updated to SUCCESS after sending the email.
   * @throws MessagingException
   */
  @Test
  void processNotificationSuccess() throws MessagingException {

    //Given
    var notification = notifications.get(0);
    var recipient = notification.getRecipient();
    var subject = "Order Placed";
    var messageBody = notification.getMessage();
    var orderId = notification.getOrderId();

    //mock send email to not throw exception
    doNothing().when(emailNotificationService).sendEmail(recipient, subject, messageBody, orderId);
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    //when
    emailNotificationService.processNotification(recipient, subject, messageBody, orderId);

    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    verify(notificationRepository, times(2)).save(notificationArgumentCaptor.capture());
    List<Notification> capturedNotifications = notificationArgumentCaptor.getAllValues();
    Notification initialNotification = capturedNotifications.get(0);
    Notification finalNotification = capturedNotifications.get(1);
    assertEquals(initialNotification.getOrderId(), finalNotification.getOrderId());
    assertEquals(Notification.Status.SUCCESS, initialNotification.getStatus());
    assertEquals(Notification.Status.SUCCESS, finalNotification.getStatus());
  }

  /**
   * Tests the processNotification method for failed email sending.
   * Verifies that the notification status is updated to FAILED after a MessagingException is thrown.
   * @throws MessagingException
   */
  @Test
  void processNotificationFailed() throws MessagingException {
    //Given
    var notification = notifications.get(1);
    var recipient = notification.getRecipient();
    var subject = "Order Cancelled";
    var messageBody = notification.getMessage();
    var orderId = notification.getOrderId();

    //mock send email to throw exception
    doThrow(new MessagingException("Simulated email sending failure"))
            .when(emailNotificationService).sendEmail(recipient, subject, messageBody, orderId);
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    //when
    emailNotificationService.processNotification(recipient, subject, messageBody, orderId);

    ArgumentCaptor<Notification> notificationArgumentCaptor = ArgumentCaptor.forClass(Notification.class);

    verify(notificationRepository, times(2)).save(notificationArgumentCaptor.capture());
    List<Notification> capturedNotifications = notificationArgumentCaptor.getAllValues();
    Notification initialNotification = capturedNotifications.get(0);
    Notification finalNotification = capturedNotifications.get(1);
    assertEquals(initialNotification.getOrderId(), finalNotification.getOrderId());
    assertEquals(Notification.Status.FAILED, initialNotification.getStatus());
    assertEquals(Notification.Status.FAILED, finalNotification.getStatus());
  }

  /**
   * Tests the getChannel method to ensure it returns the correct notification channel.
   */
  @Test
  void listenToOrderPlacedSuccess() throws MessagingException {
    // Given
    var event = new OrderPlacedEvent(
            1L,
            notifications.get(0).getRecipient(),
            "dummyToken",
            "123456"
    );

    doNothing().when(emailNotificationService).sendEmail(anyString(), anyString(), anyString(), anyLong());
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(0));

    // When
    emailNotificationService.listenToOrderPlaced(event);

    // Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  /**
   *Tests the listenToOrderPlaced method for failed email sending.
   * Verifies that the notification status is updated to FAILED after a MessagingException is thrown.
   * @throws MessagingException
   */
  @Test
  void listenToOrderPlacedFailed() throws MessagingException {
    // Given
    var event = new OrderPlacedEvent(
            2L,
            notifications.get(1).getRecipient(),
            "dummyToken",
            "654321"
    );

    doThrow(new MessagingException("Simulated email sending failure"))
            .when(emailNotificationService).sendEmail(anyString(), anyString(), anyString(), anyLong());
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(1));

    // When
    emailNotificationService.listenToOrderPlaced(event);

    // Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  /**
   * Tests the getChannel method to ensure it returns the correct notification channel.
   */
  @Test
  void listenToOrderCancelledSuccess() throws MessagingException {
    // Given
    var event = new OrderCancelledEvent(
            1L,
            notifications.get(0).getRecipient(),
            "dummyToken",
            "123456"
    );

    doNothing().when(emailNotificationService).sendEmail(anyString(), anyString(), anyString(), anyLong());
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(0));

    // When
    emailNotificationService.listenToOrderCancelled(event);

    // Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  /**
   *Tests the listenToOrderCancelled method for failed email sending.
   * Verifies that the notification status is updated to FAILED after a MessagingException is thrown.
   * @throws MessagingException
   */
  @Test
  void listenToOrderCancelledFailed() throws MessagingException {
    // Given
    var event = new OrderCancelledEvent(
            2L,
            notifications.get(1).getRecipient(),
            "dummyToken",
            "654321"
    );

    doThrow(new MessagingException("Simulated email sending failure"))
            .when(emailNotificationService).sendEmail(anyString(), anyString(), anyString(), anyLong());
    when(notificationRepository.save(any(Notification.class))).thenReturn(notifications.get(1));

    // When
    emailNotificationService.listenToOrderCancelled(event);

    // Then
    verify(notificationRepository, times(2)).save(any(Notification.class));
  }

  /**
   * Tests the getChannel method to ensure it returns the correct notification channel.
   */
  @Test
  void testSendEmailSuccess() throws MessagingException {
    // Given
    var notification = notifications.get(0);
    var to = notification.getRecipient();
    var subject = "Test Subject";
    var messageBody = "Test Message Body";
    var orderId = notification.getOrderId();

    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    when(mailProperties.getFrom()).thenReturn("testNotReply@email.com");
    doNothing().when(mailSender).send(mimeMessage);
    // When
    emailNotificationService.sendEmail(to, subject, messageBody, orderId);
    // Then
    verify(mailSender, times(1)).send(mimeMessage);
  }
}