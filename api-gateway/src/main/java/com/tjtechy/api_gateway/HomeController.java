/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the api-gateway module of the Ecommerce Microservices project.
 */
package com.tjtechy.api_gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

/**
 * HomeController is a Spring WebFlux controller that handles requests to the root URL ("/").
 * It redirects users to the Swagger UI documentation page for the API Gateway.
 * When users access the root URL, they are redirected to "/swagger-ui/index.html" to view the API documentation.
 */
@Controller
public class HomeController {
  @GetMapping("/")
  public ResponseEntity<Void> home() {
    return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("/swagger-ui/index.html"))
            .build();
  }
}
