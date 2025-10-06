/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.kafka;

import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderDeletedEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
import com.tjtechy.events.orderEvent.OrderUpdatedEvent;
import com.tjtechy.order_service.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {
  private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final KafkaTopicsProperties kafkaTopicsProperties;

  public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties kafkaTopicsProperties) {
    this.kafkaTemplate = kafkaTemplate;
    this.kafkaTopicsProperties = kafkaTopicsProperties;
  }


  /**
   * Asynchronous event sending to avoid blocking the main thread
   * @param topic
   * @param event
   * @param <T>
   */
  private <T> void sendOrderEvent(String topic, T event){
    kafkaTemplate.send(topic, event)
            .whenComplete((result, ex) -> {
              if(ex != null){
                logger.error("Error sending {} to topic {}: {}", event.getClass().getSimpleName(), topic, ex.getMessage(), ex);
              } else {
                logger.info(" {} sent to topic {} partition {} offset {}",
                        event.getClass().getSimpleName(),
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
              }
            });
    //ensure the record is flushed immediately
    kafkaTemplate.flush();
  }

  /**
   * Synchronous send to ensure order of events are maintained
   * @param topic
   * @param event
   * @param <T>
   */
  private <T> void sendOrderEventSyncChronous(String topic, T event){
    try{
      var result = kafkaTemplate.send(topic, event).get(); //blocks until send is complete
      kafkaTemplate.flush(); //ensures record is immediately sent
      logger.info("{} successfully sent to topic {} partition {} offset {} ",
              event.getClass().getSimpleName(),
              topic,
              result.getRecordMetadata().partition(),
              result.getRecordMetadata().offset());
    }
    catch(Exception e){
      logger.error("Failed to send {} to topic {} : {}",
              event.getClass().getSimpleName(),
              topic,
              e.getMessage(),
              e);
    }
  }


  /**
   * Sends OrderPlacedEvent to the appropriate Kafka topic
   * @param event
   * Order placement logic is an asynchronous operation, so we can use async send
   */
  public void sendOrderPlacedEvent(OrderPlacedEvent event){
    sendOrderEvent(kafkaTopicsProperties.getOrderPlaced(), event);
  }

  /**
   * Sends OrderCancelledEvent to the appropriate Kafka topic
   * @param event
   * Cancellation logic is a synchronous operation, so we use sync send
   */
  public void sendOrderCancelledEvent(OrderCancelledEvent event){
    sendOrderEventSyncChronous(kafkaTopicsProperties.getOrderCancelled(), event);
  }

  /**
   * Sends OrderDeletedEvent to the appropriate Kafka topic
   * @param event
   * Deletion logic is a synchronous operation, so we use sync send
   */
  public void sendOrderDeletedEvent(OrderDeletedEvent event){
    sendOrderEventSyncChronous(kafkaTopicsProperties.getOrderDeleted(), event);
  }

  /**
   * Sends OrderUpdatedEvent to the appropriate Kafka topic
   * @param event
   * Update logic is an asynchronous operation, so we can use async send
   */
  public void sendOrderUpdatedEvent(OrderUpdatedEvent event) {
    sendOrderEvent(kafkaTopicsProperties.getOrderUpdated(), event);
  }
}
