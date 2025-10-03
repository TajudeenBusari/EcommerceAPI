/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.kafka;

import com.tjtechy.events.orderEvent.OrderCancelledEvent;
import com.tjtechy.events.orderEvent.OrderPlacedEvent;
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


  public void sendOrderPlacedEvent(OrderPlacedEvent event){
    sendOrderEvent(kafkaTopicsProperties.getOrderPlaced(), event);
  }

  public void sendOrderCancelledEvent(OrderCancelledEvent event){
    sendOrderEventSyncChronous(kafkaTopicsProperties.getOrderCancelled(), event);
  }
}
