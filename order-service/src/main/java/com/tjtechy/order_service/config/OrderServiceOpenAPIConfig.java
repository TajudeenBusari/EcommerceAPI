/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of Order-service of the Ecommerce Microservices project.
 */

package com.tjtechy.order_service.config;

import com.tjtechy.config.OpenApiProperties;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OrderServiceOpenAPIConfig {

  private final OpenApiProperties openApiProperties;
  public OrderServiceOpenAPIConfig(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }
  /**
   * This is used to configure the OpenAPI documentation for the Order Service.
   */
   @Bean
   public OpenAPI orderServiceAPI() {
     return new OpenAPI()
             .info(new Info().title("Order Service API")
             .description("REST API documentation for the Order Service in the Ecommerce Microservices project")
             .version("V 1.0.0")
                     .license(new License().name("Apache 2.0"))
             .termsOfService("https://www.tjtechy.com/terms")) //dummy terms of service URL
             .externalDocs(new ExternalDocumentation()
                     .description("This is the description of an order service")
                     .url("https://www.tjtechy.com/order-service")) // dummy URL for external documentation
             .servers(List.of(
                     new Server().url(openApiProperties.getGatewayUrl()).description("Access via API Gateway"),
                     new Server().url(openApiProperties.getServiceUrl()).description("Direct access to Order Service")));
   }

   /**
    * Detects if the application is running inside a Docker container.
    * This logic has somewhat eliminated the Cors issue when accessing the service via the API Gateway in Docker.
    *
    */
//   private boolean isRunningInDocker(){
//     String env = System.getenv("RUNNING_IN_DOCKER");
//     return env != null && System.getenv("HOSTNAME") != null && System.getenv("HOSTNAME")
//         .startsWith("api-gateway");
//   }
}
