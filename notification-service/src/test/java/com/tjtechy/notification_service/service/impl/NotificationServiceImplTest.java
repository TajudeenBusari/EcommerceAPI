package com.tjtechy.notification_service.service.impl;

import com.tjtechy.notification_service.entity.Notification;
import com.tjtechy.notification_service.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class) //Enable Mockito annotations
class NotificationServiceImplTest {
  @Mock
  private NotificationRepository notificationRepository;

  private NotificationServiceImpl notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new NotificationServiceImpl(
            notificationRepository,
            "UTC",
            30);
  }

  /**
   * Test the getAllNotifications method to ensure it
   * retrieves all notifications from the repository.
   */
  @Test
  void testGetAllNotificationsSuccess(){
    //Arrange
    Notification notification1 = new Notification(
            1L,
            101L,
            "Message 1",
            "customer@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL);

    Notification notification2 = new Notification(
            2L,
            102L,
            "Message 2",
            "",
            Notification.Status.FAILED,
            LocalDate.now(),
            Notification.Channel.EMAIL);

    //Mock the repository to return a list of notifications
    when(notificationRepository.findAll()).thenReturn(List.of(notification1, notification2));
    //Act
    var notifications = notificationService.getAllNotifications();
    //Assert
    assertThat(notifications).isNotNull();
    assertThat(notifications.size()).isEqualTo(2);
    assertThat(notifications).contains(notification1, notification2);
    verify(notificationRepository, times(1)).findAll();
  }

  /**
   * Test the getNotificationById method to ensure it
   * retrieves a notification by its ID.
   */
  @Test
  void testGetNotificationByIdSuccess(){
    //Arrange
    Long notificationId = 1L;
    Notification notification = new Notification(
            notificationId,
            101L,
            "Message 1",
            "",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL);
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
    //Act
    var fetchedNotification = notificationService.getNotificationById(notificationId);
    //Assert
    assertThat(fetchedNotification).isNotNull();
    assertThat(fetchedNotification.getNotificationId()).isEqualTo(notificationId);
    verify(notificationRepository, times(1)).findById(notificationId);
  }

  /**
   * Test the removeNotification method to ensure it
   * deletes a notification by its ID.
   */
  @Test
  void testRemoveNotificationSuccess(){
    //Arrange
    Long notificationId = 1L;
    Notification notification = new Notification(
            notificationId,
            101L,
            "Message 1",
            "customer@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL);
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
    doNothing().when(notificationRepository).deleteById(notificationId);

    //Act
    notificationService.removeNotification(notificationId);

    //Assert
    verify(notificationRepository, times(1)).findById(notificationId);
    verify(notificationRepository, times(1)).deleteById(notificationId);
  }

  /**
   * Test the bulkRemoveNotificationsBySentAtLessThanEqual method to ensure it
   * deletes notifications sent on or before a specific date.
   */
  @Test
  void testBulkRemoveNotificationsBySentAtLessThanEqualSuccess(){
    //Arrange

    Long deletedCount = 5L;
    when(notificationRepository.deleteAllBySentAtLessThanEqual(any(LocalDate.class))).thenReturn(deletedCount);

    //Act
    boolean result = notificationService.bulkRemoveNotificationsBySentAtLessThanEqual();

    //Assert
    assertThat(result).isTrue();
    verify(notificationRepository, times(1)).deleteAllBySentAtLessThanEqual(any(LocalDate.class));
  }

  /**
   * Test the bulkRemoveNotificationsBySentAtLessThanEqual method to ensure it
   * returns false when there are no notifications to delete.
   */
  @Test
  void testBulkRemoveNotificationsBySentAtLessThanEqualNoNotificationToDelete(){
    //Arrange
    LocalDate date = LocalDate.now().minusDays(30);
    when(notificationRepository.deleteAllBySentAtLessThanEqual(date)).thenReturn(0L);

    //Act
    boolean result = notificationService.bulkRemoveNotificationsBySentAtLessThanEqual();

    //Assert
    assertThat(result).isFalse();
    verify(notificationRepository, times(1)).deleteAllBySentAtLessThanEqual(date);
  }

  /**
   * Test the bulkRemoveNotification method to ensure it
   * deletes multiple notifications by their IDs.
   */
  @Test
  void testBulkRemoveNotificationsSuccess(){
    //Arrange
    Notification notification1 = new Notification(
            1L,
            101L,
            "Message 1",
            "customer@email.com",
            Notification.Status.SUCCESS,
            LocalDate.now(),
            Notification.Channel.EMAIL);

    Notification notification2 = new Notification(
            2L,
            102L,
            "Message 2",
            "",
            Notification.Status.FAILED,
            LocalDate.now(),
            Notification.Channel.EMAIL);
    List<Long> notificationIds = List.of(1L, 2L);
    when(notificationRepository.findAllById(notificationIds)).thenReturn(List.of(notification1, notification2));
    doNothing().when(notificationRepository).deleteAll(List.of(notification1, notification2));

    //Act
    notificationService.bulkRemoveNotification(notificationIds);

    //Assert
    verify(notificationRepository, times(1)).deleteAll(List.of(notification1, notification2));
  }
}