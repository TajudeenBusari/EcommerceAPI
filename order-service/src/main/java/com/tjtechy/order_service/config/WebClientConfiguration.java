package com.tjtechy.order_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The Product service will be called using this web client.
 */
@Configuration
public class WebClientConfiguration {

  @Bean
  //@LoadBalanced
  public WebClient.Builder getWebClientBuilder() {
    return WebClient.builder();
  }
}
