/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test") // Uncomment this line if you have a specific test profile
@TestPropertySource(properties = {
				"api.endpoint.base-url=/api/v1",
				"spring.cloud.config.enabled=false",//disable spring cloud config
				"eureka.client.enabled=false",//disable eureka client
				"spring.datasource.url=jdbc:tc:postgresql:15.0:///orderdb",
				"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
				"spring.datasource.username=testuser",
				"spring.datasource.password=testpassword",
				"redis.enabled=false", //disable redis
				"spring.profiles.active=test"
})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
