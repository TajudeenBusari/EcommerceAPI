/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the order-service module of the EcommerceMicroservices project.
 */
package com.tjtechy.order_service.controller;

import org.apache.kafka.clients.producer.ProducerConfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;



import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {

  @Bean
  KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"
    ))); // Provide a mock or null producer factory for testing
  }
}
