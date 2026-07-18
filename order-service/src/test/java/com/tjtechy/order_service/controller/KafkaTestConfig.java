/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the order-service module of the EcommerceMicroservices project.
 */
package com.tjtechy.order_service.controller;

import com.tjtechy.order_service.config.KafkaTopicsProperties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;


import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {

  //since we are not using the @SpringBootTest in the Kafka Integration test which normally loads
  // the kafka topic from the application.yml file, we will create a bean of the kafka topic properties here
  @Bean
  KafkaTopicsProperties kafkaTopicsProperties() {
    KafkaTopicsProperties kafkaTopicsProperties = new KafkaTopicsProperties();
    kafkaTopicsProperties.setOrderPlaced("order-placed-test-topic");
    kafkaTopicsProperties.setOrderCancelled("order-cancelled-test-topic");
    kafkaTopicsProperties.setOrderUpdated("order-updated-test-topic");
    kafkaTopicsProperties.setOrderDeleted("order-deleted-test-topic");
    return kafkaTopicsProperties;
  }


  @Bean
  ProducerFactory<String, Object> producerFactory(
          @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

    Map<String, Object> configs = new HashMap<>();

    configs.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapServers);

    configs.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);

    configs.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JacksonJsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(configs);
  }

  @Bean
  KafkaTemplate<String, Object> kafkaTemplate(
          ProducerFactory<String, Object> producerFactory) {

    return new KafkaTemplate<>(producerFactory);
  }
}
