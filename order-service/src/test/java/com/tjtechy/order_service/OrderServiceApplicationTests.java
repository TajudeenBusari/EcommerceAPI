/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"api.endpoint.base-url=/api/v1",
		"spring.cloud.config.enabled=false",//disable spring cloud config
		"eureka.client.enabled=false",//disable eureka client
})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
