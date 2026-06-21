package com.tjtechy.api_gateway.Config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(GatewayOpenApiProperties.class)
public class GatewayOpenAPIConfig {


  @Bean
  public OpenAPI apiGatewayOpenAPI(GatewayOpenApiProperties openApiProperties) {

    return new OpenAPI()
            .info(new Info()
                    .title("API Gateway")
                    .description("API Gateway for Ecommerce Microservices project. " +
                            "This gateway routes requests to various microservices " +
                            "and aggregates their API documentation for a unified developer experience.")
                    .version("V 1.0.0")
                            .license(new License()
                                    .name("Apache 2.0"))
                    .contact(new Contact()
                            .name("TJTechy")
                            .url("https://www.tjtechy.com"))
                    .termsOfService("https://www.tjtechy.com/terms"))
            .externalDocs(new ExternalDocumentation()
            .description("This is the description of a API Gateway")
            .url("https://www.tjtechy.com/api-gateway"))
            .servers(List.of(
                    new Server()
                            .url(openApiProperties.getGatewayUrl())
                            .description("Access via API Gateway")
            ));
  }
}
