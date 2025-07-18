/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * This file is necessary to run the product-service integration tests
 * but not the unit tests.
 * It is used to confirm that the application context can be loaded.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
				"api.endpoint.base-url=/api/v1",
				"spring.cloud.config.enabled=false",//disable spring cloud config
				"eureka.client.enabled=false",//disable eureka client
				"spring.datasource.url=jdbc:tc:postgresql:15.0:///productdb",
				"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
				"spring.datasource.username=testuser",
				"spring.datasource.password=testpassword",
				"redis.enabled=false", //disable redis
				"spring.profiles.active=test"
})
class ProductServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
