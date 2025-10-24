package com.tjtechy.product_service.config;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This is the WebClient configuration class for the Product Service.
 * It provides a load-balanced WebClient.Builder bean that can be used
 * to create WebClient instances for making HTTP requests to other services.
 * Without this configuration, the Product Service would not be able to
 * communicate with other services like inventory-service or any other service in a load-balanced manner.
 * For example, when creating a product with inventory, the Product Service needs to call
 * the Inventory Service to check and reserve inventory for the product being created.
 */
@Configuration
public class WebClientConfig {

  /**
   * This bean provides a load-balanced WebClient.Builder
   * that can be used to create WebClient instances for making HTTP requests
   * to other services in a load-balanced manner.
   * The ObservationRegistry is used to enable observability features
   * such as metrics and tracing for the WebClient.So when the product service
   * makes calls to other services (like inventory in the case of Create product with inventory),
   * those calls can be monitored and traced, and the full request flow can be observed in the observability tools
   * like Jaeger.Without this ObservationRegistry, traces and spans are seen individually in each service
   * in Jaeger without correlation.
   * @param registry
   * @return
   */
  @Bean
  @LoadBalanced
  public WebClient.Builder webClientBuilder(ObservationRegistry registry) {

    return WebClient.builder().observationRegistry(registry);
  }

  //default WebClient bean
  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder.build();
  }

}
