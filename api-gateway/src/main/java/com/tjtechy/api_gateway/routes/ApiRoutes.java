/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the Api-gateway module of the Ecommerce Microservices project.
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
            //swagger config route
            .route("swagger-config", r->r.path("/api-docs/swagger-config")
                    .filters(f -> f.rewritePath("/api-docs/swagger-config", "/v3/api-docs/swagger-config"))
            .uri("http://localhost:8080"))

            //PRODUCT SERVICE
            .route("product-service", r -> r.path("/api/v1/product/**")
                    .uri("lb://PRODUCT-SERVICE")) //Service is registered with Eureka
            .route("product-service-docs", r -> r.path("/aggregate/product-service/v3/api-docs")
                    .filters(f -> f.rewritePath("/aggregate/product-service/v3/api-docs", "/v3/api-docs"))
                    .uri("lb://PRODUCT-SERVICE"))

            //ORDER SERVICE
            .route("order-service", r -> r.path("/api/v1/order/**")
                    .uri("lb://ORDER-SERVICE"))
            .route("order-service-docs", r -> r.path("/aggregate/order-service/v3/api-docs")
                    .filters(f -> f.rewritePath("/aggregate/order-service/v3/api-docs", "/v3/api-docs"))
                    .uri("lb://ORDER-SERVICE"))

            //INVENTORY SERVICE
            .route("inventory-service", r -> r.path("/api/v1/inventory/**")
                    .uri("lb://INVENTORY-SERVICE"))
            .route("inventory-service-docs", r -> r.path("/aggregate/inventory-service/v3/api-docs")
                    .filters(f -> f.rewritePath("/aggregate/inventory-service/v3/api-docs", "/v3/api-docs"))
                    .uri("lb://INVENTORY-SERVICE"))

            //NOTIFICATION SERVICE
            .route("notification-service", r -> r.path("/api/v1/notification/**")
                    .uri("lb://NOTIFICATION-SERVICE"))
            .route("notification-service-docs", r -> r.path("/aggregate/notification-service/v3/api-docs")
                    .filters(f -> f.rewritePath("/aggregate/notification-service/v3/api-docs", "/v3/api-docs"))
                    .uri("lb://NOTIFICATION-SERVICE"))

            //USER SERVICE/Authentication service
            .route("user-service", r -> r.path("/api/v1/user/**", "/api/v1/auth/**")
                    .uri("lb://USER-SERVICE"))
            .route("user-service-docs", r -> r.path("/aggregate/user-service/v3/api-docs")
                    .filters(f -> f.rewritePath("/aggregate/user-service/v3/api-docs", "/v3/api-docs"))
                    .uri("lb://USER-SERVICE"))

            //PAYMENT SERVICE

            .build();
  }
}
