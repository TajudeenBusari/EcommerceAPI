/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.inventory_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPIConfig configures the OpenAPI documentation for the Inventory Service.
 * //NOTE: This config class will be moved to a common util library in future to avoid code duplication when
 * //all microservices are upgraded from webmvc to webflux.
 *
 */
@Configuration

public class InventoryServiceOpenAPIConfig {

  @Bean
  public OpenAPI inventoryService() {

    return new OpenAPI()
        .info(new Info()
            .title("Inventory Service API")
            .description("API documentation for the Inventory Service of the Ecommerce Microservices project.")
                .version("V 1.0.0")
                .license(new License()
                    .name("Apache 2.0"))
            .termsOfService("https://www.tjtechy.com/terms")) // dummy terms of service URL
        .externalDocs(new ExternalDocumentation()
        .description("This is the description of an inventory service")
            .url("https://www.tjtechy.com/inventory-service"))// dummy URL for external documentation
            .servers(List.of(
                    new Server()
                            .url("/")
                            .description("Current host")
            ))
            // Define JWT Bearer authentication
            .components(new Components()
                    .addSecuritySchemes("BearerAuth",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")));

  }
}

