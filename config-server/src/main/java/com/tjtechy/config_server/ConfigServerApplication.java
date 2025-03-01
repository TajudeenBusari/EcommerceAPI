package com.tjtechy.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient // This annotation is used to enable service discovery (Eureka) in the application.
public class ConfigServerApplication {
  public static void main(String[] args) {

    SpringApplication.run(ConfigServerApplication.class, args);
  }
}