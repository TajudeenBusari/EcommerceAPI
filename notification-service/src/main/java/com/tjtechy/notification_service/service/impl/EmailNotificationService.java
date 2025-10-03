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
import com.tjtechy.notification_service.config.MailProperties;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.ChannelNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailNotificationService implements ChannelNotificationService {

  private final NotificationRepository notificationRepository;
  private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
  //spring will autoconfigure this bean based on the properties defined in application.yml
  private final JavaMailSender mailSender;
  private final MailProperties mailProperties;

  public EmailNotificationService(NotificationRepository notificationRepository, JavaMailSender mailSender, MailProperties mailProperties) {
    this.notificationRepository = notificationRepository;
    this.mailSender = mailSender;
    this.mailProperties = mailProperties;
  }


  /**
   * @param recipient
   * @param subject
   * @param messageBody
   * @param orderId
   * @throws Exception
   */
  @Override
  public void processNotification(String recipient, String subject, String messageBody, Long orderId) {
    var notification = new Notification(
            null,
            orderId,
            messageBody,
            recipient,
            Notification.Status.SUCCESS,
            LocalDate.now(),
            getChannel()
    );
    notificationRepository.save(notification);
    //send email and update status accordingly
    try{
      sendEmail(recipient, subject, messageBody, orderId);
      notification.setStatus(Notification.Status.SUCCESS);

    }catch(Exception e){
      logger.error("Failed to send email for order {}: {}", orderId, e.getMessage());
      notification.setStatus(Notification.Status.FAILED);
    }
    notificationRepository.save(notification);
  }

  /**
   * @return
   */
  @Override
  public Notification.Channel getChannel() {
    return Notification.Channel.EMAIL;
  }

  /**
   * Helper method to send email using JavaMailSender
   * @param to
   * @param subject
   * @param messageBody
   * @param orderId
   * @throws MessagingException
   */
  public void sendEmail(String to, String subject, String messageBody, Long orderId) throws MessagingException {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
    mimeMessageHelper.setTo(to);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setFrom(mailProperties.getFrom());
    String htmlContent = """
            <html>
              <body>
                <h2 style="color: #2E86C1;">%s</h2>
                <p>Dear Customer,</p>
                <p>%s</p>
                <p><strong>Order ID:</strong> %d</p>
                <br/>
                <p style="font-size: 12px; color: gray;">Thank you for shopping with us!</p>
              </body>
            </html>
            """.formatted(subject, messageBody, orderId);
    mimeMessageHelper.setText(htmlContent, true);
    mailSender.send(mimeMessage);
    logger.info("Email sent to {} regarding order ID {}", to, orderId);
  }

  /**
   * Listens to order placed events from Kafka topic and processes email notification.
   * he groupId is set to email-notification-group to ensure to differentiate from other notification services,
   * e.g., push notification service. The default value is defined in application.yml: notification-service-group
   * @param event
   *
   */
  @KafkaListener(topics = "${spring.kafka.topics.order-placed}", groupId = "email-notification-group")
  public void listenToOrderPlaced(OrderPlacedEvent event){
    logger.info("Received order placed event: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been placed successfully.";
    processNotification(event.customerEmail(), "Order Placed Confirmation", message, event.orderId());
  }

  /**
   * Listens to order canceled events from Kafka topic and processes email notification
   * The groupId is set to email-notification-group to ensure to differentiate from other notification services,
   * e.g., push notification service. The default value is defined in application.yml: notification-service-group
   * @param event
   */
  @KafkaListener(topics= "${spring.kafka.topics.order-cancelled}", groupId = "email-notification-group")
  public void listenToOrderCancelled(OrderCancelledEvent event){
    logger.info("Received order cancelled event: {}", event);
    String message = "Your order with ID " + event.orderId() + " has been cancelled.";
    processNotification(event.customerEmail(), "Order Cancellation Notice", message, event.orderId());
  }
}
