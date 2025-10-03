/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */
package com.tjtechy.notification_service.service;

import com.tjtechy.notification_service.entity.Notification;

public interface ChannelNotificationService {

  void processNotification(String to, String subject, String messageBody, Long orderId) throws Exception;
  Notification.Channel getChannel();
}
