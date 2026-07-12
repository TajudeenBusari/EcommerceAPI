/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the test-helper module of the EcommerceMicroservices project.
 */
package com.tjtechy.test_helper.security;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TestJwtGenerator {
  private final JwtEncoder jwtEncoder;

  public TestJwtGenerator(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  public String adminToken(){
    return generateToken("admintest", List.of("ROLE_ADMIN"));
  }

  public String userToken(){
    return generateToken("usertest", List.of("ROLE_USER"));
  }
  //if there are other roles, add them here


  private String generateToken(String username, List<String> authorities) {

    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("test-suite")
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plus(2, ChronoUnit.HOURS))
            .claim("authorities", authorities)
            .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

}
