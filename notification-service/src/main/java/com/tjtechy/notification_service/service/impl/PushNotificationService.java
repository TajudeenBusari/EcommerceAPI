/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.ChannelNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService implements ChannelNotificationService {
  private final NotificationRepository notificationRepository;
  private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

  public PushNotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * @param to
   * @param subject
   * @param messageBody
   * @param orderId
   * @throws Exception
   */
  @Override
  public void processNotification(String to, String subject, String messageBody, Long orderId) {
    var notification = new Notification(
            null,
            orderId,
            messageBody,
            to,
            Notification.Status.SUCCESS,
            java.time.LocalDate.now(),
            getChannel()
    );
    notificationRepository.save(notification);
    try{
      Message message = Message.builder()
              .setToken(to) //the FCM (Firebase Cloud messaging ) device token, a unique identifier that Firebase Cloud Messaging
              // generates for each client app instance(mobile app or web app)
              .setNotification(com.google.firebase.messaging.Notification.builder()
                      .setTitle(subject)
                      .setBody(messageBody)
                      .build())
              .build();
      String response = FirebaseMessaging.getInstance().send(message);
      logger.info("Successfully sent push notification message: {}",  response);
      notification.setStatus(Notification.Status.SUCCESS);

    } catch (Exception e) {
      logger.error("Failed to send push notification for order {}: {}", orderId, e.getMessage());
    }
    notificationRepository.save(notification);
  }

  /**
   * @return
   */
  @Override
  public Notification.Channel getChannel() {
    return Notification.Channel.PUSH_NOTIFICATION;
  }

  /**
   * Listens to order placed events from Kafka topic and processes push notification
   * The groupId is set to push-notification-group to ensure to differentiate from other notification services,
   * e.g., email notification service. The default value is defined in application.yml: notification-service-group
   * @param event
   */
  @KafkaListener(topics = "${spring.kafka.topics.order-placed}", groupId = "push-notification-group")
  public void listenOrderPlaced(OrderPlacedEvent event) {
    logger.info("Received order placed event: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been placed successfully!";
    processNotification(event.customDeviceToken(), "Order Placed", message, event.orderId());
  }

  /**
   * Listens to order canceled events from Kafka topic and processes push notification
   * The groupId is set to push-notification-group to ensure to differentiate from other notification services,
   * e.g., email notification service. The default value is defined in application.yml: notification-service-group
   * @param event
   */
  @KafkaListener(topics = "${spring.kafka.topics.order-cancelled}", groupId = "push-notification-group")
  public void listenToOrderCancelled(OrderCancelledEvent event) {
    logger.info("Received order cancelled event: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been cancelled.";
    processNotification(event.customDeviceToken(), "Order Cancelled", message, event.orderId());
  }
}
