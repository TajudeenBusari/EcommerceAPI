
/**
 * Copyright © 2025  
 * @Author = TJTechy (Tajudeen Busari)  
 * @Version = 1.0  
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.  
 */

package com.tjtechy.notification_service.service.impl;

import com.tjtechy.modelNotFoundException.NotificationNotFoundException;
import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import com.tjtechy.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService<Object> {

  private final NotificationRepository notificationRepository;
  private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);


  private final String timeZone;
  private final int retentionDays;


  public NotificationServiceImpl(NotificationRepository notificationRepository,

                                 @Value("${app.scheduling.timezone:UTC}") String timeZone,
                                 @Value("${app.scheduling.retention-days:30}") int retentionDays) {

    this.notificationRepository = notificationRepository;
    this.timeZone = timeZone;
    this.retentionDays = retentionDays;
  }

  /**
   * @param event
   * This is implementation of the listen method from the NotificationService interface.
   * It currently does not handle any events directly. Instead, specific event types like OrderPlacedEvent
   * and OrderCancelledEvent have their own dedicated methods for handling.
   */
  @Override
  public void listen(Object event) {
  }

  /**
   * @return
   */
  @Override
  @Cacheable(value = "notifications")
  public List<Notification> getAllNotifications() {

    return notificationRepository.findAll();
  }

  /**
   * @param id
   * @return
   */
  @Override
  @Cacheable(value = "notification", key = "#id")
  public Notification getNotificationById(Long id) {
    var notification = notificationRepository.findById(id)
            .orElseThrow(() ->
            new NotificationNotFoundException(id));
    return notification;
  }

  /**
   * @param id
   */
  @Override
  public void removeNotification(Long id) {
    notificationRepository.findById(id).orElseThrow(() ->
            new NotificationNotFoundException(id));
    notificationRepository.deleteById(id);
  }

  /**
   *Tests the bulk removal of notifications older than or equal to 30 days.
   */
  @Override
  @Transactional
  /**
   *
   * Transactional annotation ensures that the delete operation is executed within a transaction.
   * If any part of the operation fails, the transaction will be rolled back to maintain data integrity.
   * Without this annotation, if an error occurs during the deletion process, partial deletions could occur,
   * leading to inconsistent data states.
   * Transaction is essential for bulk operations to ensure atomicity and consistency.
   * NOTE:For a single delete operation: deleteById or deleteAll(), it's not strictly necessary to use @Transactional
   * because these operations are typically atomic by themselves.
   * Same logic applies to bulk save operations, bulk updates and single save, single update etc
   * respectively.
   */
  public boolean bulkRemoveNotificationsBySentAtLessThanEqual() {

    //first assert that the date is 30 days or older
    LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);

    //Check if there are any notifications older than or equal to 30 days ago.
    long count = notificationRepository.deleteAllBySentAtLessThanEqual(cutoffDate);
    if (count == 0) {
      log.info("No notifications older than or equal to {} days found for deletion.", cutoffDate);
      return false;
    }
    log.info("Deleted {} notifications older than or equal to {} days.", count, cutoffDate);
    return true;
  }

  /**
   * Automatically removes notifications that are older than 30 days or older.
   * Runs daily at midnight.
   * TODO: This method will not need a controller logic but I will expose the deleted count with
   * actuator metrics. This way we can clean up jobs in a standardized way (prometheus + grafana, etc)
   * This does not need a controller endpoint/method.
   */
  @Override
  @Transactional
  @Scheduled(cron = "0 0 0 * * ?", zone = "${app.scheduling.timezone:UTC}") //Runs daily at midnight
  public void autoRemoveOldNotifications() {
    LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
    long deletedCount = notificationRepository.deleteAllBySentAtLessThanEqual(cutoffDate);

    if (deletedCount > 0) {
      log.info("Scheduled Task: Deleted {} notifications older than {} days.", deletedCount, cutoffDate);
    } else {
      log.info("Scheduled Task: No notifications older than 30 days found for deletion.");
    }
  }

  /**
   * @param ids
   */
  @Override
  @Transactional
  public void bulkRemoveNotification(List<Long> ids) {
    var notifications = notificationRepository.findAllById(ids);
    var foundIds = notifications.stream().map(Notification::getNotificationId).toList();
    //collect ids that were not found
    var notFoundIds = ids.stream().filter(id -> !foundIds.contains(id)).toList();
    //delete all found notifications
    if (!notifications.isEmpty()) {
      notificationRepository.deleteAll(notifications);
    }
    if (!notFoundIds.isEmpty()) {
      throw new RuntimeException(" Notifications ids not found: " + notFoundIds);
    }
  }
}
