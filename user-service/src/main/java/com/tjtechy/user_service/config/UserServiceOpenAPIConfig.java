/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the User Service module of the EcommerceMicroservices project.
 */
package com.tjtechy.user_service.config;

//import com.tjtechy.config.OpenApiProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
//@EnableConfigurationProperties(OpenApiProperties.class)
public class UserServiceOpenAPIConfig {

    //private final OpenApiProperties openApiProperties;

//    public UserServiceOpenAPIConfig(OpenApiProperties openApiProperties) {
//
//        this.openApiProperties = openApiProperties;
//    }

    /**
     * OpenAPIConfig configures the OpenAPI documentation for the User Service.
     *
     */
    @Bean
    public OpenAPI userService() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .version("1.0.0")
                        .description("API documentation for the User Service of the Ecommerce Microservices project.")
                        .version("V 1.0.0")
                        .license(new License()
                                .name("Apache 2.0"))
                        .termsOfService("https://www.tjtechy.com/terms")) // dummy terms of service URL
                .externalDocs(new ExternalDocumentation()
                        .description("This is the description of a user service")
                        .url("https://www.tjtechy.com/user-service"))// dummy URL for external documentation

                .servers(List.of(
                new Server()
                        .url("/")
                        .description("Current host")
                ))// Define JWT Bearer authentication
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}




