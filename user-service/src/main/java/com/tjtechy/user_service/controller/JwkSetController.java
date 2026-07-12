package com.tjtechy.user_service.controller;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class JwkSetController {

  private final JWKSet jwkSet;

  //Bean of JWKSet is being provided by the SecurityConfiguration class
  public JwkSetController(JWKSet jwkSet) {
    this.jwkSet = jwkSet;
  }

  @GetMapping("/oauth2/jwks")
  public Mono<Map<String, Object>> getJwkSet() {
    return Mono.just(jwkSet.toPublicJWKSet().toJSONObject());
  }
}
