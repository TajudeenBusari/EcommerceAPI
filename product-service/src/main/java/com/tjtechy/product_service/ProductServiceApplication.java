/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service;

import com.tjtechy.RedisCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.Import;

@EnableCaching
@SpringBootApplication(scanBasePackages = "com.tjtechy") //	This is to scan the common-utils package for the ProductDto class and RedisCacheConfig class
@EnableDiscoveryClient
@Import(RedisCacheConfig.class)

public class ProductServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
