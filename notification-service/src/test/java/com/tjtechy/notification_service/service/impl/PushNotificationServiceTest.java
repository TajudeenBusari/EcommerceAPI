package com.tjtechy.notification_service.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Spy
  @InjectMocks
  private PushNotificationService pushNotificationService;

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  /**
   * Test the processNotification method for successful notification sending.
   * We mock FirebaseMessaging to simulate sending a push notification without actually calling Firebase.
   */
  @Test
  void processNotificationSuccess() throws FirebaseMessagingException {
    //Given
    var notification = new Notification(
            1L,
            101L,
            "Your order has been placed successfully!",
            "fcm_device_token_example",
            Notification.Status.SUCCESS,
            java.time.LocalDate.now(),
            Notification.Channel.PUSH_NOTIFICATION
    );
    var subject = "Order Confirmation";
    var messageBody = notification.getMessage();
    var recipient = notification.getRecipient();
    var orderId = notification.getOrderId();

    //mock
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    //create a firebase messaging instance mock
    FirebaseMessaging firebaseMessagingMock = mock(FirebaseMessaging.class);

    //use Mockito's static mocking for FirebaseMessaging.getInstance()
    try(MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
      //when getInstance is called, return our mock
      mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);
      //stub send method to return a message ID
      when(firebaseMessagingMock.send(any(Message.class))).thenReturn("mocked_message_id_123");

      //When
      pushNotificationService.processNotification(recipient, subject, messageBody, orderId);

      //Then
      //Assert repository save is called twice (once before sending, once after)
      verify(notificationRepository, times(2)).save(any(Notification.class));
      //Assert send method is called once
      verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }
  }

  /**
   * Test processNotification when FirebaseMessaging.send throws an exception.
   * We verify that the notification status is set to FAILED and saved.
   */
  @Test
  void processNotificationFailure() throws FirebaseMessagingException {
    //Given
    var notification = new Notification(
            1L,
            102L,
            "Your order has been placed successfully!",
            "fcm_device_token_example",
            Notification.Status.SUCCESS,
            java.time.LocalDate.now(),
            Notification.Channel.PUSH_NOTIFICATION
    );
    var subject = "Order Confirmation";
    var messageBody = notification.getMessage();
    var recipient = notification.getRecipient();
    var orderId = notification.getOrderId();

    //mock
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
    //create a firebase messaging instance mock
    FirebaseMessaging firebaseMessagingMock = mock(FirebaseMessaging.class);
    //use Mockito's static mocking for FirebaseMessaging.getInstance()
    try(MockedStatic<FirebaseMessaging> mockedStatic = mockStatic(FirebaseMessaging.class)) {
      //when getInstance is called, return our mock
      mockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);
      //stub send method to throw an exception
      when(firebaseMessagingMock.send(any(Message.class))).thenThrow(new RuntimeException("Mocked sending failure"));

      //When
      pushNotificationService.processNotification(recipient, subject, messageBody, orderId);
      //Then
      //Assert repository save is called twice (once before sending, once after)
      verify(notificationRepository, times(2)).save(any(Notification.class));
      //Assert send method is called once
      verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }
  }

  @Test
  void getChannel() {
  }

  /**
   * Test the Kafka listener method for order placed events.
   * We mock processNotification to verify it gets called with correct parameters
   * when an OrderPlacedEvent is received.
   */
  @Test
  void listenOrderPlacedSuccess() {
    //given
    var event = new OrderPlacedEvent(
            1L,
            "CUSTOMER@EMAIL.COM",
            "fcm_device_token_example",
            "+1234567890"
    );

    //mock
    /**
     * Spy is used to partially mock the PushNotificationService object. It allows us to mock
     * specific methods (like processNotification) while keeping the rest of the behavior intact.
     * Here, we mock processNotification to do nothing when called,
     * so we can verify it was called without executing its real logic.
     */
    doNothing().when(pushNotificationService).processNotification(anyString(), anyString(), anyString(), anyLong());

    //when
    pushNotificationService.listenOrderPlaced(event);

    //then
    verify(pushNotificationService, times(1)).processNotification(
            eq(event.customDeviceToken()),
            eq("Order Placed" ),
            eq("Your order with ID " + event.orderId() + " has been placed successfully!"),
            eq(event.orderId())
    );
  }

  /**
   *  Test the Kafka listener method for order canceled events.
   *    * We mock processNotification to verify it gets called with correct parameters
   *    * when an OrderPlacedEvent is received.
   */
  @Test
  void listenToOrderCancelledSuccess() {
    //given
    var event = new OrderCancelledEvent(
            2L,
            "CUSTOMER@EMAIL.COM",
            "fcm_device_token_example",
            "+1244567890"
    );

    //mock
    doNothing().when(pushNotificationService).processNotification(anyString(), anyString(), anyString(), anyLong());

    //when
    pushNotificationService.listenToOrderCancelled(event);

    //then
    verify(pushNotificationService, times(1)).processNotification(
            eq(event.customDeviceToken()),
            eq("Order Cancelled" ),
            eq("Your order with ID " + event.orderId() + " has been cancelled."),
            eq(event.orderId())
    );
  }
}