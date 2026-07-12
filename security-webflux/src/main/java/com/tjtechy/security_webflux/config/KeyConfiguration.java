package com.tjtechy.security_webflux.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableConfigurationProperties(RsaKeyProperties.class)
public class KeyConfiguration {

  private final RsaKeyProperties rsaKeyProperties;

  public KeyConfiguration(RsaKeyProperties rsaKeyProperties){
    this.rsaKeyProperties = rsaKeyProperties;
  }

  @Bean
  public RSAPublicKey publicKey() throws IOException {
    try (InputStream inputStream = rsaKeyProperties.getPublicKey().getInputStream()){
      return RsaKeyConverters.x509().convert(inputStream);
    }
  }

  @Bean
  public RSAPrivateKey privateKey() throws IOException {
    try(InputStream inputStream = rsaKeyProperties.getPrivateKey().getInputStream()){
      return RsaKeyConverters.pkcs8().convert(inputStream);
    }
  }
}
