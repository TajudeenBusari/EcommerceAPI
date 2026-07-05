/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service;

import com.tjtechy.RedisCacheConfig;

import com.tjtechy.actuator.ActuatorConfiguration;
import com.tjtechy.actuator.CustomBeansEndpoint;
import com.tjtechy.actuator.CustomUsableMemoryHealthIndicator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


@EnableCaching
@SpringBootApplication(scanBasePackages = "com.tjtechy") // This is to scan the common-utils package for Inventory and RedisCacheConfig class
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.tjtechy") // This is to scan the common-utils package for the Inventory class
@EntityScan(basePackages = "com.tjtechy") // This is to scan the common-utils package for the Inventory class
@Import({RedisCacheConfig.class,
        ActuatorConfiguration.class,
        CustomBeansEndpoint.class,
        CustomUsableMemoryHealthIndicator.class})


public class InventoryServiceApplication {

  static void main(String[] args) {
    SpringApplication.run(InventoryServiceApplication.class, args);
  }

  /**
   * This method is used to print all the request mapping information for all the controllers in the application.
   * @return
   * The error seen in the console is a default error message from Spring Boot when the application is started.
   * It is not an error in the code:
   * [Controller]{ [/error], produces [text/html]} -> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#errorHtml(HttpServletRequest, HttpServletResponse)
   * [Controller]{ [/error]} -> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#error(HttpServletRequest)
   */
  @Bean
  public ApplicationRunner applicationRunner(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
    return args -> mapping.getHandlerMethods()
            .forEach((requestMappingInfo, handlerMethod) ->
                    System.out.println("[Controller]" + requestMappingInfo + " -> " + handlerMethod));
  }
}