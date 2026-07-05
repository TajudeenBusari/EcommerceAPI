/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the User Service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigureLogger {
  private static final Logger logger = LoggerFactory.getLogger(ConfigureLogger.class);

  @Value("${app.admin.username}")
  private String username;

  @Value("${app.admin.password}")
  private String password;

  @PostConstruct
  public void logConfig(){
    logger.info("Admin username from configuration: {}", username);
    //JUST FOR DEBUGGING, DON'T LOG PASSWORD
    //logger.info("Admin password from configuration: {}", password);
  }
}
