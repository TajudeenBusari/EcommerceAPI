/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.service.impl;

import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.ChannelNotificationService;
import com.tjtechy.notification_service.service.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SMSNotificationService implements ChannelNotificationService {
  private final NotificationRepository notificationRepository;
  /**
   * There are many implementations of SmsProvider interface:
   * - TwilioSmsProvider: Integrates with Twilio's SMS API to send real SMS messages.
   * - DummySmsProvider: A mock implementation that simulates sending SMS messages for testing purposes.
   * - AwsSnsSmsProvider: Uses AWS SNS (Simple Notification Service) to send SMS messages(not implemented yet).
   * - NexmoSmsProvider: Integrates with Nexmo (now Vonage) SMS API for sending messages(not implemented yet).
   * You can choose any of these implementations based on your requirements and configuration.
   * Twilio is marked as primary in the TwilioSmsProvider, so it will be used by default unless specified otherwise.
   */
  private final SmsProvider smsProvider;
  private static final Logger logger = LoggerFactory.getLogger(SMSNotificationService.class);

  public SMSNotificationService(NotificationRepository notificationRepository, SmsProvider smsProvider) {
    this.notificationRepository = notificationRepository;
    this.smsProvider = smsProvider;
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
    try {
      smsProvider.sendSms(to, messageBody);
      logger.info("Sent SMS to {}: {}", to, messageBody);
    } catch (Exception e) {
      logger.error("Failed to send SMS to {}: {}", to, e.getMessage());
      notification.setStatus(Notification.Status.FAILED);
    }
    notificationRepository.save(notification);
  }

  /**
   * @return
   */
  @Override
  public Notification.Channel getChannel() {
    return Notification.Channel.SMS;
  }

  @KafkaListener(topics = "${spring.kafka.topics.order-placed}", groupId = "sms-notification-group")
  public void listenToOrderPlaced(OrderPlacedEvent event) {
    logger.info("Received OrderPlacedEvent: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been placed successfully!";
    processNotification(event.customerPhoneNumber(), "Order Placed", message, event.orderId());
  }

  @KafkaListener(topics = "${spring.kafka.topics.order-cancelled}", groupId = "sms-notification-group")
  public void listenToOrderCancelled(OrderCancelledEvent event) {
    logger.info("Received OrderCancelledEvent: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been cancelled.";
    processNotification(event.customerPhoneNumber(), "Order Cancelled", message, event.orderId());
  }
}
