/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.topics")
public class KafkaTopicsProperties {
  @NotBlank(message = "kafka topic 'order-placed' must not be blank")
  private String orderPlaced;
  @NotBlank(message = "kafka topic 'order-cancelled' must not be blank")
  private String orderCancelled;
  @NotBlank(message = "kafka topic 'payment-failed' must not be blank")
  private String paymentFailed;
  @NotBlank(message = "kafka topic 'payment-received' must not be blank")
  private String paymentReceived;

  public String getOrderPlaced() {
    return orderPlaced;
  }
  public void setOrderPlaced(String orderPlaced) {
    this.orderPlaced = orderPlaced;
  }

  public String getOrderCancelled() {
    return orderCancelled;
  }
  public void setOrderCancelled(String orderCancelled) {
    this.orderCancelled = orderCancelled;
  }

  public String getPaymentFailed() {
    return paymentFailed;
  }
  public void setPaymentFailed(String paymentFailed) {
    this.paymentFailed = paymentFailed;
  }

  public String getPaymentReceived() {
    return paymentReceived;
  }
  public void setPaymentReceived(String paymentReceived) {
    this.paymentReceived = paymentReceived;
  }
}
