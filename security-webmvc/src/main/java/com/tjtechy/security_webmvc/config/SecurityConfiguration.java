package com.tjtechy.security_webmvc.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfiguration {

//  private final RSAPublicKey publicKey;
//  private final RSAPrivateKey privateKey;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Bean
  public JwtAuthenticationConverter customJwtAuthenticationConverter(){
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

    grantedAuthoritiesConverter.setAuthorityPrefix(""); //remove default "SCOPE_" prefix

    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtAuthenticationConverter;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                    .requestMatchers(HttpMethod.GET, this.baseUrl + "/product").permitAll() //allow unauthenticated access to GET /product endpoint
                    .requestMatchers(HttpMethod.GET, this.baseUrl + "/product/**").permitAll() //allow unauthenticated access to GET /product/{id} endpoint
                    .requestMatchers(HttpMethod.POST, this.baseUrl + "/product").hasAuthority("ROLE_ADMIN") //allow only ADMIN role to access POST /product endpoint
                    .requestMatchers(HttpMethod.POST, this.baseUrl + "/product/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers(HttpMethod.PUT, this.baseUrl + "/product/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/product/**").hasAuthority("ROLE_ADMIN")
                    .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll() //allow unauthenticated access to health and info endpoints
                    .requestMatchers(EndpointRequest.toAnyEndpoint().excluding("health", "info", "prometheus")).hasAuthority("ROLE_ADMIN") //allow only ADMIN role to access other actuator endpoints
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()//allow unauthenticated access to swagger endpoints for API documentation
                    .anyRequest()
                    .authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable) //disable to allow post, put, delete requests without csrf token
            .cors(Customizer.withDefaults())
            .httpBasic(AbstractHttpConfigurer::disable) //disable basic auth
            .oauth2ResourceServer(oauth2 ->
                    oauth2.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter())))//configure the JWT authentication converter to extract authorities from the JWT token

            //disable session management as we are using JWT (stateless JWT tokens)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
  }
}
