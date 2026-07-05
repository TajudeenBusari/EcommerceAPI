/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of system module of the Ecommerce Microservices project.
 */

package com.tjtechy.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Custom actuator endpoint to count the number of beans in the application context.
 * The endpoint is accessible at /actuator/customs-beans
 * @Component indicates that this class is a Spring-managed component.
 * @Endpoint defines a custom actuator endpoint with the specified id.
 * @ReadOperation indicates that this method handles read operations for the endpoint.
 * The countBeans method retrieves the total number of bean definitions in the application context
 * and returns it as an integer.
 */
@Component
@Endpoint(id = "customs-beans")
public class CustomBeansEndpoint {
  private final ApplicationContext applicationContext;

  public CustomBeansEndpoint(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @ReadOperation
  public int countBeans() {
    return applicationContext.getBeanDefinitionCount();
  }
}
