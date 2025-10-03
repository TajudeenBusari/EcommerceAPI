/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {
  private static final Logger logger = LoggerFactory.getLogger(KafkaTopicConfig.class);

//  @Value("${kafka.topics.order-placed}")
//  private String orderPlacedTopic;
//
//  @Value("${kafka.topics.order-cancelled}")
//  private String orderCancelledTopic;
  private final KafkaTopicsProperties kafkaTopicsProperties;

  public KafkaTopicConfig(KafkaTopicsProperties kafkaTopicsProperties) {
    this.kafkaTopicsProperties = kafkaTopicsProperties;
  }

  /***
   * Creates a topic named order-placed-topic and order-cancelled-topic with 3 partitions.
   * If topic already exists, Kafka ignores it.
   *
   */

//  @Bean
//  public KafkaAdmin.NewTopics topics() {
//    logger.info("Creating Kafka topics: '{}' and '{}'", orderPlacedTopic, orderCancelledTopic);
//    return new KafkaAdmin.NewTopics(
//            TopicBuilder.name(orderPlacedTopic)
//                    .partitions(3)
//                    .replicas(1)
//                    .build(),
//            TopicBuilder.name(orderCancelledTopic)
//                    .partitions(3)
//                    .replicas(1)
//                    .build()
//    );
//  }

  @Bean
  public KafkaAdmin.NewTopics topics() {
    logger.info("Creating Kafka topics: '{}', '{}', '{}'",
            kafkaTopicsProperties.getOrderPlaced(),
            kafkaTopicsProperties.getOrderCancelled(),
            kafkaTopicsProperties.getPaymentFailed()
    );
    return new KafkaAdmin.NewTopics(
            TopicBuilder.name(kafkaTopicsProperties.getOrderPlaced())
                    .partitions(3)
                    .replicas(1)
                    .build(),
            TopicBuilder.name(kafkaTopicsProperties.getOrderCancelled())
                    .partitions(3)
                    .replicas(1)
                    .build(),
            TopicBuilder.name(kafkaTopicsProperties.getPaymentFailed())
                    .partitions(3)
                    .replicas(1)
                    .build()
    );
  }

  @EventListener(ApplicationReadyEvent.class)
  public void logTopics() {
    logger.info("Resolved topic names: '{}', '{}', '{}'", kafkaTopicsProperties.getOrderPlaced(), kafkaTopicsProperties.getOrderCancelled(), kafkaTopicsProperties.getPaymentFailed());
  }
}
