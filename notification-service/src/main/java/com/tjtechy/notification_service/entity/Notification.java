/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Table(name = "notifications")
@Entity
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;
  private Long orderId;
  //private Long userId;
  private String message;
  private String recipient;
  private LocalDate sentAt;

  @Enumerated(EnumType.STRING)
  private Status status; //// SUCCESS, FAILED, PENDING

  @Enumerated(EnumType.STRING)
  private Channel channel; //// EMAIL, SMS, PUSH


  public Notification() {
  }
  public Notification(Long notificationId, Long orderId, String message, String recipient, Status status, LocalDate sentAt, Channel channel) {
    this.notificationId = notificationId;
    this.message = message;
    this.recipient = recipient;
    this.status = status;
    this.sentAt = sentAt;
    this.channel = channel;
    this.orderId = orderId;
  }
  public Long getNotificationId() {
    return notificationId;
  }
  public void setNotificationId(Long notificationId) {
    this.notificationId = notificationId;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public String getRecipient() {
    return recipient;
  }
  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }
  public Status getStatus() {
    return status;
  }
  public void setStatus(Status status) {
    this.status = status;
  }
  public LocalDate getSentAt() {
    return sentAt;
  }
  public void setSentAt(LocalDate sentAt) {
    this.sentAt = sentAt;
  }
  public Channel getChannel() {
    return channel;
  }
  public void setChannel(Channel channel) {
    this.channel = channel;
  }
  public Long getOrderId() {
    return orderId;
  }
  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }
  @Override
  public String toString() {
    return "Notification{" +
            "notificationId=" + notificationId +
            ", orderId=" + orderId +
            ", message='" + message + '\'' +
            ", recipient='" + recipient + '\'' +
            ", status='" + status + '\'' +
            ", sentAt=" + sentAt +
            ", channel='" + channel + '\'' +
            '}';
  }

  //Add some validation for channel and status
  public boolean isStatusValid(String status) {
    if(status == null) return false;
    return List.of("SUCCESS", "FAILED", "PENDING").contains(status.trim().toUpperCase());
  }

  public enum Channel {
    EMAIL,
    SMS,
    PUSH_NOTIFICATION
  }

  public enum Status {
    SUCCESS,
    FAILED,
    PENDING
  }
}
