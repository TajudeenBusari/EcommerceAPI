/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the eureka-server module of the Ecommerce Microservices project.
 */
package com.tjtechy.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
@EnableEurekaServer

public class EurekaServerApplication {

	static void main(String[] args) {

		SpringApplication.run(EurekaServerApplication.class, args);
	}

}
