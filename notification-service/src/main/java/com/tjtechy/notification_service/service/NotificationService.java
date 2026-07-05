/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of notification-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.notification_service.service;


import com.tjtechy.notification_service.entity.Notification;


import java.util.List;

public interface NotificationService {
  //void listen(T event);
  List<Notification> getAllNotifications();
  Notification getNotificationById(Long id);
  void removeNotification(Long notificationId);
  boolean bulkRemoveNotificationsBySentAtLessThanEqual();
  void autoRemoveOldNotifications();
  void bulkRemoveNotification(List<Long> notificationIds);
}
