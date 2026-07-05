/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the system module of the Ecommerce Microservices project.
 */

package com.tjtechy.actuator;

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *Controls how http exchanges are stored and managed for actuator endpoints.
 * In this case, it uses an in-memory repository with a capacity of 1000 exchanges.
 * @Configuration indicates that this class contains Spring bean definitions.
 * @Bean indicates that the method returns a bean to be managed by the Spring container.
 * The actuator endpoint is: /actuator/httpexchanges
 * InMemoryHttpExchangeRepository store httpexchanges traces in memory (RAM), by default, it is set to 1000.
 * So older traces will be evicted when the limit is reached.
 * With this, we can see:
 * method (GET, POST etc.)
 * response status (200, 404 etc.)
 * request and response headers
 * time taken to process the request
 * The data provided by this endpoint can be useful for monitoring and debugging purposes
 * and can also be persisted into a database, file or send to monitoring service for long-term analysis.
 * NOTE: Check ONENOTE FOR EXAMPLE USAGE
 * TODO: Secure endpoint when spring security is added
 */
@Configuration
public class ActuatorConfiguration {
  @Bean
  public HttpExchangeRepository httpExchangeRepository() {
    var repo = new InMemoryHttpExchangeRepository();
    repo.setCapacity(1000); // Set the capacity to 1000 exchanges
    return repo;
  }
}
