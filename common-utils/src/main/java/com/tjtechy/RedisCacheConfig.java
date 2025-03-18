/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 *Configuration class for setting up Redis cache.
 * This class defines a {@link RedisCacheManager} bean that is responsible for configuring Redis cache
 * and a default TTL (Time To Live) of 1 minute.
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisCacheConfig {

  private static final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

  @PostConstruct
  public void init() {
    logger.info("****Redis cache configuration initialized****.");
    logger.debug("Redis cache configuration initialized.");
  }

  /** // Redis cache configuration
   * Configures and returns a {@link RedisCacheManager} for managing Redis base caching.
   * <p>
   *   This configuration:
   *   <ul>
   *     <li>Uses Jackson for serialization.</li>
   *     <li>Registers {@link JavaTimeModule} to handle {@code LocalDateTime} serialization.</li>
   *     <li>Sets a default cache expiration time of 1 minute.</li>
   *     <li>Disables caching of null values.</li>
   *   </ul>
   * </p>
   * @param redisConnectionFactory Redis connection factory
   * @return A fully configured {@link RedisCacheManager} instance.
   */
  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

    // custom object with JavaTimeModule
    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule()); // Enables LocalDateTime serialization
    objectMapper.activateDefaultTyping(
        objectMapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY
    ); // Enables type information

    // Create serializer with the ObjectMapper in constructor
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

    // Redis cache configuration
    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(1))
        .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer())//Ensures readable keys
            )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)
        );
    logger.info("✅**** Using Jackson2JsonRedisSerializer for Redis caching****");
    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(redisCacheConfiguration)
        .build();
  }
}
