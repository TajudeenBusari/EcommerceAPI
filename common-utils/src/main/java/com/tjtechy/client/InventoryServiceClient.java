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
import com.tjtechy.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryServiceClient {

  private final WebClient.Builder clientBuilder;
  private static final Logger logger = LoggerFactory.getLogger(InventoryServiceClient.class);

  /**
   * The base URL for the Inventory Service.
   * This is typically set in the application.yml of the service calling
   * the Inventory Service.
   * It should point to the base URL of the Inventory Service API.
   * x-service-url is used by external clients to access the x-Service.
   * api.endpoint.base-url is used by the service itself to access its own endpoints.
   */
  //@Value("${api.endpoint.base-url}")
  @Value("${inventory-service.base-url}")
  private String inventoryServiceUrl;


  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  /**
   * Deducts inventory for a given product by the specified quantity.
   * This method is called by the Order Service (in this case, the client) when an order is placed.
   * @param productId the UUID of the product
   * @param quantity  the quantity to deduct from inventory
   * @return a Mono that completes when the inventory deduction is successful
   */
  public Mono<Void> deductInventory(UUID productId, Integer quantity) {
    var deductInventoryRequestDto = new DeductInventoryRequestDto(productId, quantity);
    //var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/internal/deduct-inventory-reactive";
    var url = inventoryServiceUrl + "/inventory/internal/deduct-inventory-reactive";
    return clientBuilder.build()
            .patch()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(deductInventoryRequestDto)
            .retrieve()
            .bodyToMono(Result.class)
            .flatMap(inventoryResponse -> {
              if(inventoryResponse == null || !inventoryResponse.isFlag()) {
                return Mono.error(new IllegalArgumentException("Failed to deduct inventory"));
              }
              return Mono.empty();
            });
  }

  /**
   * Asynchronously creates inventory for a product.
   * This method is called when a new product is added by the product-service (client) to the system.
   * @param createInventoryDto the DTO containing the product ID and initial stock
   */
  public void createInventoryForProductAsync(CreateInventoryDto createInventoryDto) {

//    var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/internal/create";
    var url = inventoryServiceUrl + "/inventory/internal/create";

    var savedProductId = createInventoryDto.productId();
    clientBuilder.build()
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createInventoryDto)
            .retrieve()
            .bodyToMono(Result.class)
            .subscribe(response -> {
              if(response != null && response.isFlag()) {
                logger.info("*******Inventory created successfully for product {}*******", savedProductId);
              } else {
                logger.warn("*******Inventory creation returned a failure response for product {}: {}*******",
                        savedProductId, response != null ? response.getMessage() : "No message");
              }
            }, error -> {
              logger.error("*******Error occurred while creating inventory for product {}: {}*******",
                      savedProductId, error.getMessage());
            });
    logger.info("Calling inventory Service to create inventory: {}", url);
  }

  /**
   * Restores inventory for a given product by the specified quantity.
   * This method is typically called when an order is cancelled, returned, or updated,
   * and the inventory needs to be restored.
   * @param productId the UUID of the product
   * @param quantityToRestore the quantity to restore to inventory
   * @return a Mono that completes when the inventory restoration is successful
   */
  public Mono<Void> restoreInventory(UUID productId, Integer quantityToRestore) {

    // Validate inputs
    if (productId == null || quantityToRestore == null || quantityToRestore <= 0) {
      return Mono.error(new IllegalArgumentException("Invalid product ID or quantity to restore"));
    }
    var restoreInventoryDto = new RestoreInventoryDto(productId, quantityToRestore);
//    var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/internal/restore-inventory";
    var url = inventoryServiceUrl + "/inventory/internal/restore-inventory";
    return clientBuilder.build()
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(restoreInventoryDto)
            .retrieve()
            .bodyToMono(Result.class)
            .timeout(Duration.ofSeconds(5)) // Set a timeout for the request
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))// Retry with backoff
            .flatMap(inventoryResponse -> {
              if(inventoryResponse == null || !inventoryResponse.isFlag()) {
                return Mono.error(new IllegalArgumentException("Failed to restore inventory"));
              }
              return Mono.empty(); //Returns Mono<Void> to indicate completion and not a data response
            });
  }


  public Mono<InventoryDto> getInventoryByProductId(UUID productId) {
    if (productId == null) {
      return Mono.error(new IllegalArgumentException("Product ID cannot be null"));
    }
//    var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/internal/product/" + productId;
    var url = inventoryServiceUrl + "/inventory/internal/product/" + productId;
    return clientBuilder.build()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(Result.class)
            .flatMap(result -> {
              if (result != null && result.isFlag() && result.getData() != null) {
                //extract inventoryId from the response
                var inventoryDto = objectMapper.convertValue(result.getData(), InventoryDto.class);
                return Mono.just(inventoryDto);
              }else {
                return Mono.error(new IllegalArgumentException("Failed to retrieve inventory for productId: " + productId));
              }
            });


  }


  public Mono<Result> updateInventory(Long inventoryId, UpdateInventoryDto updateInventoryDto) {
    //var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/internal/update/" + inventoryId;
    var url = inventoryServiceUrl + "/inventory/internal/update/" + inventoryId;

    return clientBuilder.build()
            .put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateInventoryDto)
            .retrieve()
            .bodyToMono(Result.class);
  }


  public Mono<Result> deleteInventory(Long inventoryId) {
    if (inventoryId == null) {
      return Mono.error(new IllegalArgumentException("Inventory ID cannot be null"));
    }
    //var url = "http://inventory-service" + inventoryServiceUrl + "/inventory/" + inventoryId;
    var url = inventoryServiceUrl + "/inventory/" + inventoryId;
    return clientBuilder.build()
            .delete()
            .uri(url)
            .retrieve()
            .bodyToMono(Result.class);
  }

}
