/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tjtechy.ProductDto;
import com.tjtechy.Result;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component //This annotation indicates that this class is a Spring component, allowing it to be auto-detected and managed by the Spring container.
public class ProductServiceClient {

  private final WebClient.Builder clientBuilder;

  //@Value("${api.endpoint.base-url}")
  @Value("${product-service.base-url}")

  private String productServiceBaseUrl;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  public ProductServiceClient(WebClient.Builder clientBuilder) {
    this.clientBuilder = clientBuilder;
  }

  public Mono<ProductDto> getProductById(UUID productId) {
//    var url = "http://product-service" + productServiceBaseUrl + "/product/" + productId;
    var url = productServiceBaseUrl + "/product/" + productId;
    return clientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            //this step handles when product is not found it is very important to handle this when
            //creating a new order with a product that does not exist in the database
            //if the statuscode for the response is 404 NOT FOUND, then it will throw a ProductNotFoundException
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
              if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                return Mono.error(new ProductNotFoundException(productId));
              }
              return clientResponse.createException().flatMap(Mono::error);
            })
            .bodyToMono(Result.class)
            .map(productResponse -> {
              if(productResponse == null || productResponse.getData() == null) {
                throw new ProductNotFoundException(productId);
              }
              // Convert LinkedHashMap to ProductDto manually
              return objectMapper.convertValue(productResponse.getData(), ProductDto.class);
            });
  }
}
