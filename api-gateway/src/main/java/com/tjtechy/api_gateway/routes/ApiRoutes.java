/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.api_gateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApiRoutes {

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
            //PRODUCT SERVICE
            .route("product-service", r -> r.path("/api/v1/products/**")
                    .uri("lb://PRODUCT-SERVICE")) //Service is registered with Eureka
            //ORDER SERVICE
            .route("order-service", r -> r.path("/api/v1/orders/**")
                    .uri("lb://ORDER-SERVICE"))
            //INVENTORY SERVICE
            .route("inventory-service", r -> r.path("/api/v1/inventory/**")
                    .uri("lb://INVENTORY-SERVICE"))
            //USER SERVICE/Authentication service
            //PAYMENT SERVICE
            .build();
  }
}
