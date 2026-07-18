package com.tjtechy.order_service.controller;


import com.tjtechy.events.orderEvent.*;
import com.tjtechy.order_service.config.KafkaTopicsProperties;
import com.tjtechy.order_service.kafka.OrderEventProducer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
//@SpringBootTest is designed to bootstrap the application context. But we are not testing the whole application here.
@ExtendWith(SpringExtension.class)

@ContextConfiguration(
        classes = {KafkaTestConfig.class,
                OrderEventProducer.class
        }
)
@Tag("OrderServiceEventProducerIntegrationTest")
public class OrderEventProducerIntegrationTest {

  @Autowired
  private OrderEventProducer orderEventProducer;

  @Autowired
  private KafkaTopicsProperties kafkaTopicsProperties;

  private Consumer<String, OrderPlacedEvent> consumerPlaced;
  private Consumer<String, OrderCancelledEvent> consumerCancelled;
  private Consumer<String, OrderUpdatedEvent> consumerUpdated;
  private Consumer<String, OrderDeletedEvent> consumerDeleted;

  //private final ObjectMapper objectMapper = new ObjectMapper();


  @Autowired
  ApplicationContext applicationContext;

  @Container
  static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

  @DynamicPropertySource
  static void kafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    registry.add("kafka.topics.order-placed", () -> "order-placed-topic");
    registry.add("kafka.topics.order-cancelled", () -> "order-cancelled-topic");
    registry.add("kafka.topics.order-deleted", () -> "order-deleted-topic");
    registry.add("kafka.topics.order-updated", () -> "order-updated-topic");
  }

  @BeforeEach
  void setUp(){
    Map<String, Object> map = new HashMap<>();
    map.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    map.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    map.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    map.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    map.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

    consumerPlaced = new DefaultKafkaConsumerFactory<>(
            map,
            new  StringDeserializer(),
            new JacksonJsonDeserializer<>(OrderPlacedEvent.class))
            .createConsumer();

    consumerCancelled = new DefaultKafkaConsumerFactory<>(
            map,
            new StringDeserializer(),
            new JacksonJsonDeserializer<>(OrderCancelledEvent.class))
            .createConsumer();

    consumerUpdated = new DefaultKafkaConsumerFactory<>(
            map,
            new StringDeserializer(),
            new JacksonJsonDeserializer<>(OrderUpdatedEvent.class))
            .createConsumer();

    consumerDeleted = new DefaultKafkaConsumerFactory<>(
            map,
            new StringDeserializer(),
            new JacksonJsonDeserializer<>(OrderDeletedEvent.class))
            .createConsumer();

    //System.out.println("===========OrderPlaced Topic========: " + kafkaTopicsProperties.getOrderPlaced());

    consumerPlaced.subscribe(List.of(
            kafkaTopicsProperties.getOrderPlaced()
    ));
    consumerCancelled.subscribe(List.of(
            kafkaTopicsProperties.getOrderCancelled()
    ));
    consumerUpdated.subscribe(List.of(
            kafkaTopicsProperties.getOrderUpdated()
    ));
    consumerDeleted.subscribe(List.of(
            kafkaTopicsProperties.getOrderDeleted()
    ));
  }

  @AfterEach
  void tearDown(){
    consumerPlaced.close();
    consumerCancelled.close();
    consumerUpdated.close();
    consumerDeleted.close();
  }


  @DisplayName("Print all beans in the application context")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void shouldPrintBeans(){
    System.out.println(Arrays.toString(applicationContext.getBeanNamesForType(OrderEventProducer.class)));
    System.out.println(Arrays.toString(applicationContext.getBeanNamesForType(KafkaTopicsProperties.class)));
  }


  @DisplayName("Test that OrderPlacedEvent is published to Kafka topic")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void shouldPublishedOrderPlacedEvent(){

    OrderPlacedEvent expected = new OrderPlacedEvent(
            1L,
            "customer1@email.com",
            "",
            "12345678",
            LocalDate.now(),
            ActionBy.ADMIN,
            Reason.OTHER
    );

    orderEventProducer.sendOrderPlacedEvent(expected);

    ConsumerRecord<String, OrderPlacedEvent> record = KafkaTestUtils
            .getSingleRecord(consumerPlaced, kafkaTopicsProperties.getOrderPlaced());

    OrderPlacedEvent actual = record.value();

    assertThat(actual.equals(expected), org.hamcrest.Matchers.is(true));
  }

  @DisplayName("Test that OrderCancelledEvent is published to Kafka topic")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void shouldPublishedOrderCancelledEvent(){
    OrderCancelledEvent expected = new OrderCancelledEvent(
            1L,
            "customer1@email.com",
            "",
            "12345678",
            LocalDate.now(),
            ActionBy.ADMIN,
            Reason.OTHER
    );

    orderEventProducer.sendOrderCancelledEvent(expected);

    ConsumerRecord<String, OrderCancelledEvent> record = KafkaTestUtils
            .getSingleRecord(consumerCancelled, kafkaTopicsProperties.getOrderCancelled());

    OrderCancelledEvent actual = record.value();

    assertThat(actual.equals(expected), org.hamcrest.Matchers.is(true));
  }

  @DisplayName("Test that OrderUpdatedEvent is published to Kafka topic")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void shouldPublishedOrderUpdatedEvent(){
    OrderUpdatedEvent expected = new OrderUpdatedEvent(
            1L,
            "customer1@email.com",
            "",
            "12345678",
            ActionBy.ADMIN,
            Reason.OTHER,
            LocalDate.now()
    );

    orderEventProducer.sendOrderUpdatedEvent(expected);

    ConsumerRecord<String, OrderUpdatedEvent> record = KafkaTestUtils
            .getSingleRecord(consumerUpdated, kafkaTopicsProperties.getOrderUpdated());

    OrderUpdatedEvent actual = record.value();

    assertThat(actual.equals(expected), org.hamcrest.Matchers.is(true));
  }

  @DisplayName("Test that OrderDeletedEvent is published to Kafka topic")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void shouldPublishedOrderDeletedEvent(){
    OrderDeletedEvent expected = new OrderDeletedEvent(
            1L,
            "customer1@email.com",
            "",
            "12345678",
            Reason.OTHER,
            ActionBy.ADMIN,
            LocalDate.now()
    );

    orderEventProducer.sendOrderDeletedEvent(expected);

    ConsumerRecord<String, OrderDeletedEvent> record = KafkaTestUtils
            .getSingleRecord(consumerDeleted, kafkaTopicsProperties.getOrderDeleted());

    OrderDeletedEvent actual = record.value();

    assertThat(actual.equals(expected), org.hamcrest.Matchers.is(true));
  }

}
