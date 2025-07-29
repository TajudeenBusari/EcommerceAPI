/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.product_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  /**
   * This is used to configure the OpenAPI documentation for the Product Service.
   * @return
   */
  @Bean
  public OpenAPI productServiceAPI() {
    return new OpenAPI()
            .info(new Info().title("Product Service API")
            .description("REST API documentation for the Product Service in the Ecommerce Microservices project")
            .version("V 1.0.0")
                    .license(new License().name("Apache 2.0"))
            .termsOfService("https://www.tjtechy.com/terms")) //dummy terms of service URL
            .externalDocs(new ExternalDocumentation()
                    .description("This is the description of a product service")
                    .url("https://www.tjtechy.com/product-service")); // dummy URL for external documentation
  }
}
