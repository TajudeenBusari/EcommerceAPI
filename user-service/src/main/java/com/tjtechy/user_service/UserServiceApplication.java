/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.user_service;

import com.tjtechy.RedisCacheConfig;
import com.tjtechy.user_service.config.AdminProperties;

import com.tjtechy.security_webflux.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

//user-service application is based on spring webflux, so security-related beans are in security_webflux module
@SpringBootApplication(scanBasePackages = {
				"com.tjtechy.security_webflux", //needed for JwtEncoder bean, without this JwtEncoder bean is not available
				"com.tjtechy.user_service", // needed for UserServiceApplication and other beans in user-service module
				"com.tjtechy.security" // needed for PasswordEncoder bean, without this PasswordEncoder bean is not available
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
