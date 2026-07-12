/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service;

import com.tjtechy.RedisCacheConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.Import;

@EnableCaching
@SpringBootApplication(scanBasePackages = {
				"com.tjtechy", //This is to scan the common-utils package for the ProductDto class and RedisCacheConfig class
				"com.tjtechy.security_webmvc", //This is to scan the security-webmvc package for the JwtEncoder bean and other security-related beans
				"com.tjtechy.product_service" //This is to scan the product-service package for the ProductServiceApplication class and other beans in product-service module
})
@EnableDiscoveryClient
@Import(RedisCacheConfig.class)
//@EnableJpaRepositories(basePackages = "com.tjtechy.product_service.repository")

public class ProductServiceApplication {

	static void main(String[] args) {

		SpringApplication.run(ProductServiceApplication.class, args);
	}
}
