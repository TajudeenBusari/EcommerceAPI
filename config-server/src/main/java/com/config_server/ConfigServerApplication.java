/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of config-server module of the Ecommerce Microservices project.
 */
package com.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ConfigServerApplication {
  public static void main(String[] args) {

    SpringApplication.run(ConfigServerApplication.class, args);
  }
}
/**
 * Use mvn clean package to build the project in the terminal
 * in case of any error.
 */