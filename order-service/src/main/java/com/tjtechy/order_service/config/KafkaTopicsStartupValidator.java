/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the order-service module of the EcommerceMicroservices project.
 */

package com.tjtechy.order_service.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaTopicsStartupValidator {
  private static final Logger logger = LoggerFactory.getLogger(KafkaTopicsStartupValidator.class);
  private final KafkaTopicsProperties kafkaTopicsProperties;

  public KafkaTopicsStartupValidator(KafkaTopicsProperties kafkaTopicsProperties) {
    this.kafkaTopicsProperties = kafkaTopicsProperties;
  }

  @PostConstruct
  public void validateTopics() {
    logger.info("Validating Kafka topic configurations...");
    if (kafkaTopicsProperties.getOrderPlaced() == null || kafkaTopicsProperties.getOrderPlaced().isBlank()) {
      throw new IllegalStateException("Kafka topic 'order-placed' is not configured properly.");
    }
    if (kafkaTopicsProperties.getOrderCancelled() == null || kafkaTopicsProperties.getOrderCancelled().isBlank()) {
      throw new IllegalStateException("Kafka topic 'order-cancelled' is not configured properly.");
    }
    if (kafkaTopicsProperties.getOrderDeleted() == null || kafkaTopicsProperties.getOrderDeleted().isBlank()) {
      throw new IllegalStateException("Kafka topic 'order-deleted' is not configured properly.");
    }
    if (kafkaTopicsProperties.getOrderUpdated() == null || kafkaTopicsProperties.getOrderUpdated().isBlank()) {
      throw new IllegalStateException("Kafka topic 'order-updated' is not configured properly.");
    }
    //not yet implemented
    if (kafkaTopicsProperties.getPaymentFailed() == null || kafkaTopicsProperties.getPaymentFailed().isBlank()) {
      throw new IllegalStateException("Kafka topic 'payment-failed' is not configured properly.");
    }
    //not yet implemented
    if (kafkaTopicsProperties.getPaymentReceived() == null || kafkaTopicsProperties.getPaymentReceived().isBlank()) {
      throw new IllegalStateException("Kafka topic 'payment-received' is not configured properly.");
    }
    logger.info("All required Kafka topics are configured properly.");
  }
}
