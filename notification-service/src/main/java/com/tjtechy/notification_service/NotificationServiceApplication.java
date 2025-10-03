package com.tjtechy.notification_service;

import com.tjtechy.RedisCacheConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication //don't scan the common-util package, it will try to inject the InventoryServiceClient bean which will fail
@EnableDiscoveryClient
@EnableCaching
@Import(RedisCacheConfig.class)
@EnableScheduling

public class NotificationServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Bean
	public ApplicationRunner init(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
		return args -> mapping.getHandlerMethods()
		.forEach((requestMappingInfo, handlerMethod) ->
						System.out.println("[Controller]" + requestMappingInfo + " -> " + handlerMethod));
	}
}

/**
 * @SpringBootApplication(scanBasePackages = "com.tjtechy") causes trouble because
 * it includes @ComponentScan which scans all packages under com.tjtechy by default.
 * when you set scanBasePackages to "com.tjtechy", it tries to register all beans under
 * com.tjtechy, including beans that are not relevant to this service. This can lead to issues like
 * the one you encountered with InventoryServiceClient, which is not defined in the notification-service module.
 * To avoid this, you can specify the base package more narrowly to only include packages
 * that are relevant to the notification-service module, such as "com.tjtechy.notification_service".
 * Alternatively, you can exclude specific classes or packages from component scanning
 * using the excludeFilters attribute of @ComponentScan.
 * For example:
 * @ComponentScan(basePackages = "com.tjtechy", excludeFilters = {
 * @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InventoryServiceClient.class)
 * })
 * This will scan all packages under com.tjtechy except for the InventoryServiceClient class.
 * However, the simplest solution is to just remove the scanBasePackages attribute
 * if your main application class is already in the root package of your service module.
 *0R
 * @ComponetScan(basePackages = "com.tjtchy,
 * includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {Class1.class, Class2.class},
 *  useDefaultFilters = false)
 */