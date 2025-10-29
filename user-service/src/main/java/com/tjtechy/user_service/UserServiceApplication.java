package com.tjtechy.user_service;

import com.tjtechy.RedisCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConfiguration;

@SpringBootApplication(scanBasePackages = {
				"com.tjtechy.security", "com.tjtechy.user_service"
})
@EnableCaching
@EnableDiscoveryClient
@Import(RedisCacheConfig.class)
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
