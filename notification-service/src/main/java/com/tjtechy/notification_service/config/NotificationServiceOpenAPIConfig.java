/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the notification-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.config;

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
public class NotificationServiceOpenAPIConfig {
  private final OpenApiProperties openApiProperties;

  public NotificationServiceOpenAPIConfig(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }

  @Bean
  public OpenAPI notificationService() {

    return new OpenAPI()
            .info(new Info()
                    .title("Notification Service API")
                    .version("1.0.0")
                    .description("API documentation for the Notification Service of the Ecommerce Microservices project.")
                    .version("V 1.0.0")
                    .license(new License()
                            .name("Apache 2.0"))
                    .termsOfService("https://www.tjtechy.com/terms")) // dummy terms of service URL
            .externalDocs(new ExternalDocumentation()
                    .description("This is the description of a notification service")
                    .url("https://www.tjtechy.com/notification-service")) // dummy URL for external documentation
            .servers(List.of(
                    new Server().url(openApiProperties.getGatewayUrl()).description("Access via API Gateway"),
                    new Server().url(openApiProperties.getServiceUrl()).description("Direct access to Notification Service"))
            );
  }

}
