package com.tjtechy.notification_service;

import com.tjtechy.notification_service.config.FirebaseConfig;
import com.tjtechy.notification_service.service.impl.TwilioSmsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
				"api.endpoint.base-url=/api/v1",
				"spring.cache.type=none", // Disable caching for tests
				"spring.cloud.config.enabled=false", // Disable Spring Cloud Config for tests
				"spring.redis.enabled=false",// Disable Redis for tests
				"api.endpoint.base-url=/api/v1",
				"spring.cloud.config.enabled=false",//disable spring cloud config
				"eureka.client.enabled=false",//disable eureka client
				"spring.datasource.url=jdbc:tc:postgresql:15.0:///notificationdb",
				"spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
				"spring.datasource.username=testuser",
				"spring.datasource.password=testpassword",
				"redis.enabled=false", //disable redis
				"spring.profiles.active=test",
				//removed all these because was causing some connection issues in the service and controller unit test when all tests are run together.
//				"spring.kafka.bootstrap-servers=localhost:0", // Use an invalid port to prevent actual connections
//				"spring.kafka.admin.auto-create=false", // Prevent auto-creation of topics
//				"spring.kafka.consumer.auto-offset-reset=none", // Prevent consumer from auto-starting
})
class NotificationServiceApplicationTests {
	/**
	 * Mock these to prevent context load issues related to external services.
	 */
	@MockitoBean
	private TwilioSmsProvider twilioSmsProvider;
	@MockitoBean
	private FirebaseConfig firebaseConfig;

	@Test
	void contextLoads() {
	}

}
