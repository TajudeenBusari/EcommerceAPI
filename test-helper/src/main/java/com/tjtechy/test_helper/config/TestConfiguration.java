/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the test-helper module of the EcommerceMicroservices project.
 */
package com.tjtechy.test_helper.config;

import com.tjtechy.test_helper.security.TestJwtGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
@ComponentScan(basePackages = "com.tjtechy.test_helper")
//@Import(TestKeyConfiguration.class) use this or ComponentScan
public class TestConfiguration {

  //explicitly provide the Bean of TestJwtGenerator so that JwtEncoder bean is also available
  @Bean
  public TestJwtGenerator testJwtGenerator(JwtEncoder jwtEncoder) {

    return new TestJwtGenerator(jwtEncoder);
  }

  //explicitly provide the Bean of RSAPublicKey so that JwtDecoder bean is also available
  @Bean
  public JwtDecoder jwtDecoder(RSAPublicKey publicKey) {
    return NimbusJwtDecoder.withPublicKey(publicKey).build();
  }
}
