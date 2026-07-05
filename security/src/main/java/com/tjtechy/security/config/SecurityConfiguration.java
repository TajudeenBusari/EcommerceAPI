/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the security module of the Ecommerce Microservices project.
 */

package com.tjtechy.security.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;


import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name="app.security.enabled", havingValue = "true", matchIfMissing = true) //helps to disable security in tests or when not needed by setting app.security.enabled=false in application.properties
public class SecurityConfiguration {
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    public SecurityConfiguration() throws NoSuchAlgorithmException {

      //generate a public and private key pair to digitally sign the JWT tokens.
      // In production, you should use a secure way to store and manage these keys, such as using a secrets manager or environment variables.
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048); //2048 bits key size
      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      this.publicKey = (RSAPublicKey) keyPair.getPublic();
      this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

//    /**
//      * Password encoder bean using BCrypt hashing algorithm.
//      * note: BCrypt is a strong hashing algorithm that is widely used for securely storing passwords.
//      * This bean is needed when using the PasswordEncoder interface in the application.
//      * It is a one-way hashing function, meaning that once a password is hashed,
//      * it cannot be reversed back to its original form. This makes it ideal for password storage,
//      * as even if the database is compromised, the original passwords cannot be easily retrieved.
//      * a private key is used to encode, and a public key (for decoding) is used to very if it is ok.
//      * public key can be distributed to a public key server or other services
//      * that need to verify the authenticity of the JWT tokens issued by this service.
//      * private is kept as secrete, for example, in auth service.
//   */ NOTE: Already moved to PasswordConfig class to separate concerns.
//  @Bean
//  public PasswordEncoder passwordEncoder() {
//
//    return new BCryptPasswordEncoder(12); //12 is the strength parameter
//  }

  /**
   * JWT Encoder bean
   * @return JwtEncoder
   */
  @Bean
  public JwtEncoder jwtEncoder(){
    JWK jwk = new RSAKey
            .Builder(this.publicKey)
            .privateKey(this.privateKey)
            .build();
    JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSet);
  }

  /**
   * Reactive Jwt Decoder for Spring WebFlux applications.
   * Different from JwtDecoder for Spring Web Mvc applications.
   */
  @Bean
  public ReactiveJwtDecoder jwtDecoder(){

    return NimbusReactiveJwtDecoder
            .withPublicKey(this.publicKey)
            .build();
  }

  /**
   * JWT Authentication Converter bean
   * This bean is used to extract authorities from the JWT token.
   * @return JwtAuthenticationConverter
   */
  @Bean
  public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(){
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities"); //the claim name in the JWT token that contains the authorities

    //remove default scope prefix "SCOPE_" from the authorities
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); //remove default "SCOPE_" prefix

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
  }

  /**
   * Security filter chain configuration
   * Once there is a spring security dependency, by default, all endpoints are secured.
   * So make a state change like a POST, PUT, DELETE request to any endpoint will require CSRF token.
   * For simplicity, we are disabling CSRF here.
   * SecurityWebFilterChain is the equivalent of SecurityFilterChain(Spring Web Mvc) in Spring WebFlux applications.
   * User Service where we disable csrf uses Spring WebFlux.
   * Because the role is an enum, then all properties should be in capital: for example, ROLE_ADMIN.
   * When you decode the jwt, the authorities look like this: "authorities": "ROLE_ADMIN"
   *
   */
  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    return http
            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers(HttpMethod.POST, baseUrl + "/auth/login").permitAll()
                    .pathMatchers(HttpMethod.POST, baseUrl + "/user/register").hasAnyAuthority("ROLE_ADMIN")
                    .pathMatchers(HttpMethod.GET, baseUrl + "/user").hasAnyAuthority("ROLE_ADMIN")
                    .pathMatchers(HttpMethod.GET, baseUrl + "/user/**").hasAnyAuthority("ROLE_ADMIN") //TODO:to be updated with AuthorizationManager to allow users to access their own user info but not other users' info
                    .pathMatchers(HttpMethod.PUT, baseUrl + "/user/**").hasAnyAuthority("ROLE_ADMIN") //TODO:to be updated with AuthorizationManager to allow users to update their own user info but not other users' info
                    .pathMatchers(HttpMethod.DELETE, baseUrl + "/user/**").hasAnyAuthority("ROLE_ADMIN")
                    .pathMatchers(HttpMethod.PATCH, baseUrl + "/user/**").hasAnyAuthority("ROLE_ADMIN") //TODO:to be updated with AuthorizationManager to allow users to update their own user info but not other users' info

                    //permit swagger endpoints
                    .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() //allow unauthenticated access to swagger endpoints for API documentation

                    //actuator endpoints security rules
                    .pathMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll() //allow unauthenticated access to these endpoints for monitoring purposes
                    .pathMatchers("/actuator/**").hasAnyAuthority("ROLE_ADMIN") //only allow users with admin role to access other actuator endpoints
                    .anyExchange()
                    .authenticated())
            //disable csrf
            .csrf(ServerHttpSecurity.CsrfSpec::disable) //disable to allow post, put, delete requests without csrf token

            //disable cors for simplicity, but in production, you should configure it properly to allow only trusted origins
            //.cors(ServerHttpSecurity.CorsSpec::disable)
            .cors(Customizer.withDefaults())

            //disable http basic authentication as we are using JWT tokens for authentication
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

            //TODO: TO BE UPDATED WITH CUSTOM ENTRY POINT AND ACCESS-DENIED HANDLER
            //.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt ->
                            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))) //configure the JWT authentication converter to extract authorities from the JWT token

            //disable session management as we are using JWT (stateless JWT tokens)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build();
  }
}
