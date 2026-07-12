/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the test-helper module of the EcommerceMicroservices project.
 */
package com.tjtechy.test_helper.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration

public class TestKeyConfiguration {
  //when Maven builds this module (test-helper), the cert under resources folder is copied to the root
  //of the classpath and the path is hardcoded here. So this: cert/public-test.pem will be read correctly.
  private static final Resource PUBLIC_KEY = new ClassPathResource("cert/public-test.pem");
  private static final Resource PRIVATE_KEY = new ClassPathResource("cert/private-test.pem");

  @Bean
  public RSAPublicKey testPublicKey() throws Exception {
    return readPublicKey();
  }

  @Bean
  public RSAPrivateKey testPrivateKey() throws Exception {
    return readPrivateKey();
  }

 @Bean
 public JwtEncoder testJwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
   JWK jwk = new RSAKey
           .Builder(publicKey)
           .privateKey(privateKey)
           .keyID("test-key-id")
           .build();
   JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));
   return new NimbusJwtEncoder(source);
 }


  private RSAPublicKey readPublicKey() throws Exception {
    String key = new String(PUBLIC_KEY.getInputStream().readAllBytes());
    key = key.
            replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decodedKey = Base64.getDecoder().decode(key);
    return (RSAPublicKey) KeyFactory.getInstance("RSA")
            .generatePublic(new X509EncodedKeySpec(decodedKey));
  }

  private RSAPrivateKey readPrivateKey() throws Exception {
    String key = new String(PRIVATE_KEY.getInputStream().readAllBytes());
    key = key.
            replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decodedKey = Base64.getDecoder().decode(key);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
  }
}
