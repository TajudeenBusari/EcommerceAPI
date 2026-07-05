/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service;

import com.tjtechy.RedisCacheConfig;
import com.tjtechy.user_service.config.AdminProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;


@SpringBootApplication(scanBasePackages = {
				"com.tjtechy.security", "com.tjtechy.user_service"
})
@EnableCaching
@EnableDiscoveryClient
@Import(RedisCacheConfig.class)
@EnableConfigurationProperties(AdminProperties.class)
public class UserServiceApplication {

	static void main(String[] args) {

		SpringApplication.run(UserServiceApplication.class, args);
	}

}
