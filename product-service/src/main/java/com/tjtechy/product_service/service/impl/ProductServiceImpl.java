/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.service.impl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.*;
import com.tjtechy.client.InventoryServiceClient;
import com.tjtechy.product_service.config.InventoryServiceConfig;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.repository.ProductRepository;
import com.tjtechy.product_service.service.ProductService;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * This class is the implementation of the ProductService interface.
 */
@Service //create a bean of this class and //register it in the Spring context
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final InventoryServiceConfig inventoryServiceConfig;
    private final WebClient.Builder webClientBuilder;
    private final InventoryServiceClient inventoryServiceClient;

  public ProductServiceImpl(ProductRepository productRepository, InventoryServiceConfig inventoryServiceConfig, WebClient.Builder webClientBuilder, InventoryServiceClient inventoryServiceClient) {

    this.productRepository = productRepository;
    this.inventoryServiceConfig = inventoryServiceConfig;
    this.webClientBuilder = webClientBuilder;
    this.inventoryServiceClient = inventoryServiceClient;
  }

  /**
   * Retrieves all products from the database.
   * <p>
   * This method is cached under the "products" cache name. If the cache contains data,
   * it will return the cached results instead of querying the database.
   * </p>
   * <p>
   * Logging is used to track the operation:
   * <ul>
   *     <li>Logs a message before fetching products from the database.</li>
   *     <li>Logs the total number of products retrieved.</li>
   * </ul>
   * </p>
   * <p>Supported log levels include:
   * <ul>
   *     <li>{@code log.info} - General informational logs.</li>
   *     <li>{@code log.debug} - Detailed debug information.</li>
   *     <li>{@code log.warn} - Warning messages.</li>
   *     <li>{@code log.error} - Error messages.</li>
   * </ul>
   * </p>
   *
   * @return a list of all {@link Product} entities available in the database.
   */
  @Cacheable(value = "products") //store in "products" cache
  @Override
    public List<Product> getAllProducts() {
      logger.info("*******Fetching products from database*******");
      var products = productRepository.findAll();
    /**
     * Log the number of products fetched
     * log.info is used and other log levels can be used.
     * E.g., log.debug, log.error, log.warn etc.
     */
    logger.info("*******Fetched {} products*******", products.size());
      return products;
    }


    /**
     * Retrieves a product by its unique identifier.
     * <p>
     * This method is cached under the "product" cache name with the product ID as the key.
     * If the cache contains data, it will return the cached results instead of querying the database.
     * </p>
     * <p>
     * If the product is not found in the database, a {@link ProductNotFoundException} is thrown.
     * </p>
     *
     * @param productId the unique identifier of the product to retrieve.
     * @return the {@link Product} entity with the specified ID.
     * @throws ProductNotFoundException if the product with the specified ID is not found in the database.
     */
    @Cacheable(value = "product", key = "#productId") //store in "products" cache with key as id
    @Override
    public Product getProductById(UUID productId) {

      var foundProduct = productRepository
              .findById(productId)
              .orElseThrow(() -> new ProductNotFoundException(productId));

        return foundProduct;
    }

    /**
     * Saves a product to the database.
     * <p>
     * This method is cached under the "product" cache name with the product ID as the key.
     * The cache is updated with the new product value.
     * </p>
     *
     * @param product the {@link Product} entity to save.
     * @return the saved {@link Product} entity.
     */
    @CachePut(value = "product", key = "#product.productId") //store in "products" cache with key as id
    @Override
    public Product saveProduct(Product product) {
      ensureProductQuantityAndAvailableStockAreInSync(product);
      return productRepository.save(product);
    }


    /**
     * Saves a product to the database and triggers an asynchronous call to the inventory service
     * to create an inventory for the product in the inventory-service.
     * When a product is created, the available stock is set to 1 by default.
     * The other fields of the product are set to the values provided in the product object.
     * This method is deprecated and should be replaced with
     * {@link #saveProductWithInventoryUsingExternalizedService(Product)}.
     * <p>
     * At the moment, cache is not used for this method.
     * </p>
     *
     * @param product the {@link Product} entity to save.
     * @return the saved {@link Product} entity.
     */
    @Override
    @Deprecated
    public Product saveProductWithInventory(Product product) {

      //first check if the productQuantity is equal to the availableStock, else set availableStock to productQuantity
      ensureProductQuantityAndAvailableStockAreInSync(product);
      //2.save the product to the database
      var savedProduct = productRepository.save(product);

      //3.Trigger async call to inventory service to create inventory
      String inventoryServiceUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory";
      //String inventoryServiceUrl = inventoryServiceConfig.getBaseUrl();


      //By default, when the product is created, the available stock is set to 1.
      var createInventoryDto = new CreateInventoryDto(
              savedProduct.getProductId(),
              savedProduct.getAvailableStock(),
              1
      );

      webClientBuilder.build()
              .post()
              .uri(inventoryServiceUrl + "/internal/create")
              .contentType(MediaType.APPLICATION_JSON)
              .bodyValue(createInventoryDto) //this should be CreateInventoryDto
              .retrieve()
              .bodyToMono(Result.class)
              //fire and forget-->non-blocking
              .subscribe(response -> {
                if (response != null && response.isFlag()) {
                  logger.info("*******Inventory created successfully for product {}*******", savedProduct.getProductId());
              } else {
                  logger.warn("*******Inventory creation returned a failure response for product {}: {}*******",
                          savedProduct.getProductId(), response != null ? response.getMessage() : "No message");
                }
              }, error -> {
                logger.error("*******Error occurred while creating inventory for product {}: {}*******",
                        savedProduct.getProductId(), error.getMessage());
              });

      // Return the saved product regardless of the inventory creation outcome
    return savedProduct;
  }

  /**
   * Saves a product to the database and triggers an asynchronous call to the inventory service
   * using an externalized Inventory service to create an inventory for the product in the inventory-service.
   * When a product is created, the available stock is set to 1 by default.
   * The other fields of the product are set to the values provided in the product object.
   * <p>
   * At the moment, cache is not used for this method.
   * </p>
   *
   * @param product the {@link Product} entity to save.
   * @return the saved {@link Product} entity.
   */
  @Override
  public Product saveProductWithInventoryUsingExternalizedService(Product product) {


    //first check if the productQuantity is equal to the availableStock, else set availableStock to productQuantity
//    if (product.getProductQuantity() != null && product.getAvailableStock() == null) {
//      product.setAvailableStock(product.getProductQuantity());
//    } else if (product.getAvailableStock() != null && product.getProductQuantity() == null) {
//      product.setProductQuantity(product.getAvailableStock());
//    } else if (product.getProductQuantity() != null && product.getAvailableStock() != null
//            && !product.getProductQuantity().equals(product.getAvailableStock())) {
//      product.setAvailableStock(product.getProductQuantity());
//    }
    ensureProductQuantityAndAvailableStockAreInSync(product);

    //1.save the product to the database
    var savedProduct = productRepository.save(product);

    //2.Trigger async call to inventory service to create inventory using the externalized inventory service
    //By default, when the product is created, the reserved quantity is set to 1.
    /*TODO: The CreateInventoryDto should be externalized from the product-service
     *To ensure that the separation of concerns is maintained
     *Product-Service should not have too much knowledge of the Inventory-service.
     *This can be done by changing the parameter of the createInventoryForProductAsync method
     *to accept Product (you need to add product-service module as dependency to the common-module
     * or move the Product class to the common module)
     * object instead of CreateInventoryDto and create an instance of
     * CreateInventoryDto in the InventoryServiceClient class.
    */

    var createInventoryDto = new CreateInventoryDto(
            savedProduct.getProductId(),
            savedProduct.getAvailableStock(),
            1
    );
    inventoryServiceClient.createInventoryForProductAsync(createInventoryDto);

    return savedProduct;
  }


  /**
     * Updates a product with the specified ID.
     * manufacturedDate and expiryDate should not be updated.
     * <p>
     * This method is cached under the "product" cache name with the product ID as the key.
     * The cache is updated with the new product value.
     * </p>
     * <p>
     * If the product with the specified ID is not found in the database, a {@link ProductNotFoundException} is thrown.
     * </p>
     *
     * @param productId the unique identifier of the product to update.
     * @param product the updated product entity.
     * @return the updated {@link Product} entity.
     * @throws ProductNotFoundException if the product with the specified ID is not found in the database.
     */
    @CachePut(value = "product", key = "#productId") //update cache with new value
    @Override
    public Product updateProduct(UUID productId, Product product) {
        var foundProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        foundProduct.setProductName(product.getProductName());
        foundProduct.setProductCategory(product.getProductCategory());
        foundProduct.setProductDescription(product.getProductDescription());
        foundProduct.setProductPrice(product.getProductPrice());
        // If productQuantity is set, use it to update availableStock
        ensureProductQuantityAndAvailableStockAreInSync(product);
        foundProduct.setProductQuantity(product.getProductQuantity());
        foundProduct.setAvailableStock(product.getAvailableStock());
        logger.info("*******product {} updated successfully *******", productId);
        return productRepository.save(foundProduct);
    }


  /**Updates a product with the specified ID and triggers an asynchronous call to the inventory service
   * to update the inventory for the product in the inventory-service.
   * First, it retrieves the inventory ID from the inventory service using the product ID.
   * Then, it updates the inventory with the new available stock and reserved quantity.
   * By default, when the product is updated, the available stock is set to 1.
   * If the product with the specified ID is not found in the database, a 404 Not Found is thrown.
   * @param productId
   * @param product
   * @return updated product {@link Product} entity.
   */
  @Override
  public Product updateProductWithInventory(UUID productId, Product product) {
      var foundProduct = productRepository.findById(productId)
              .orElseThrow(() -> new ProductNotFoundException(productId));
      foundProduct.setProductName(product.getProductName());
      foundProduct.setProductCategory(product.getProductCategory());
      foundProduct.setProductDescription(product.getProductDescription());
      foundProduct.setProductPrice(product.getProductPrice());

      // If productQuantity is set, use it to update availableStock
      ensureProductQuantityAndAvailableStockAreInSync(product);

      foundProduct.setProductQuantity(product.getProductQuantity());
      foundProduct.setAvailableStock(product.getAvailableStock());
      var updatedProduct = productRepository.save(foundProduct);

      //1. Call the GET /inventory/product/{productId} endpoint to get the inventory ID
    String getInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/internal/product/" + productId;
    //String getInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/internal/product/" + productId;
      webClientBuilder.build()
              .get()
              .uri(getInventoryUrl)
              .retrieve()
              .bodyToMono(Result.class)
              .flatMap(result -> {
                if(result != null && result.isFlag() && result.getData() != null) {
                  //extract inventoryId from the response
                  var objectMapper = new ObjectMapper();
                  var inventoryDto = objectMapper.convertValue(result.getData(), InventoryDto.class);
                  var inventoryId = inventoryDto.inventoryId();

                  var updateInventoryDto = new UpdateInventoryDto(
                          updatedProduct.getProductId(),
                          updatedProduct.getAvailableStock(),
                          1
                  );
                  //2. Call PUT /inventory/internal/update/{inventoryId} endpoint to update the inventory
                  //Trigger async call to inventory service to update inventory
                  String updateInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/internal/update/" + inventoryId;
                  //String updateInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/internal/update/" + inventoryId;
                  return webClientBuilder.build()
                          .put()
                          .uri(updateInventoryUrl)
                          .contentType(MediaType.APPLICATION_JSON)
                          .bodyValue(updateInventoryDto) //this should be UpdateInventoryDto
                          .retrieve()
                          .bodyToMono(Result.class);
                } else {
                  // Handle the case where the inventory is not found
                  logger.warn("*******Inventory not found for productId: {}*******", productId);
                  return Mono.error(new RuntimeException("Inventory not found for productId: " + productId));
                }
              }).subscribe(response -> {
      if (response != null && response.isFlag()) {
        logger.info("*******Inventory updated successfully for product {}*******", updatedProduct.getProductId());
      } else {
        logger.warn("*******Inventory update returned a failure response for product {}: {}*******",
                updatedProduct.getProductId(), response != null ? response.getMessage() : "No message");
      }
    }, error -> {
      logger.error("*******Error occurred while updating inventory for product {}: {}*******",
              updatedProduct.getProductId(), error.getMessage());
    });

      // Return the updated product regardless of the inventory update outcome
    return updatedProduct;
  }

  @Override
  public Product updateProductWithInventoryUsingExternalizedService(UUID productId, Product product) {
    var foundProduct = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    foundProduct.setProductName(product.getProductName());
    foundProduct.setProductCategory(product.getProductCategory());
    foundProduct.setProductDescription(product.getProductDescription());
    foundProduct.setProductPrice(product.getProductPrice());
    // If productQuantity is set, use it to update availableStock
    ensureProductQuantityAndAvailableStockAreInSync(product);
    foundProduct.setProductQuantity(product.getProductQuantity());
    foundProduct.setAvailableStock(product.getAvailableStock());
    var updatedProduct = productRepository.save(foundProduct);

    //1. Call the GET /inventory/internal/product/{productId} endpoint to get the inventory ID
    inventoryServiceClient.getInventoryByProductId(productId)
            .flatMap(inventoryDto -> {
              var inventoryId = inventoryDto.inventoryId();

              var updateInventoryDto = new UpdateInventoryDto(
                      updatedProduct.getProductId(),
                      updatedProduct.getAvailableStock(),
                      1
              );
              //2. Call PUT /inventory/internal/update/{inventoryId} endpoint to update the inventory
              return inventoryServiceClient.updateInventory(inventoryId, updateInventoryDto);
            })
            .subscribe(response -> {
              if (response != null && response.isFlag()) {
                logger.info("*******Inventory is updated successfully for product {}*******", updatedProduct.getProductId());
              } else {
                logger.warn("*******Inventory update returns a failure response for product {}: {}*******",
                        updatedProduct.getProductId(), response != null ? response.getMessage() : "No message");
              }
            }, error -> {
              logger.error("*******Error occurred while updating inventory for a product {}: {}*******",
                      updatedProduct.getProductId(), error.getMessage());
            });

    // Return the updated product regardless of the inventory update outcome
    return updatedProduct;
  }

  /**
     * Deletes a product with the specified ID.
     * <p>
     * This method is cached under the "product" cache name with the product ID as the key.
     * The cache is updated to remove the product with the specified ID.
     * </p>
     * <p>
     * If the product with the specified ID is not found in the database, a {@link ProductNotFoundException} is thrown.
     * </p>
     *
     * @param productId the unique identifier of the product to delete.
     * @throws ProductNotFoundException if the product with the specified ID is not found in the database.
     */
    @CacheEvict(value = "product", key = "#productId") //delete cache with key as id
    @Override
    public void deleteProduct(UUID productId) {
      productRepository.findById(productId)
              .orElseThrow(() -> new ProductNotFoundException(productId));
      productRepository.deleteById(productId);
      logger.info("*******product {} deleted successfully *******", productId);
    }

    /**
     * Clears all cache entries.
     * <p>
     * This method is used to clear all cache entries.
     * </p>
     */
  @Override
  @CacheEvict(value = "products", allEntries = true) //delete all cache
  public void clearAllCache() {
    //Log some message here
    logger.info("*******Clearing all cache*******");
    logger.info("*******All cache cleared successfully*******");

  }

  /**
   * Deletes multiple products with the specified IDs.
   * <p>
   * This method first retrieves all products with the given IDs from the database.
   * If any of the specified product IDs are not found, a {@link ProductNotFoundException} is thrown.
   * </p>
   *
   * @param productIds the list of unique identifiers of the products to delete.
   * @throws ProductNotFoundException if any of the specified product IDs are not found in the database.
   */
  @Override
  public void bulkDeleteProducts(List<UUID> productIds) {
    var products = productRepository.findAllById(productIds);

    //extract all found product ids and collect them into a list
    var foundProductIds = products.stream()
            .map(Product::getProductId)
            .toList();

    //determine missing ids and collect them into a list
    var missingIds = productIds.stream()
            .filter(id -> !foundProductIds.contains(id))
            .toList();

    //delete only found ids
    if(!products.isEmpty()){
      productRepository.deleteAll(products);
    }

    //throw exception if there are missing ids
    if (!missingIds.isEmpty()) {
      throw new ProductNotFoundException(missingIds);
    }
  }

  @Override
  @CacheEvict(value = "product", key = "#productId") //delete cache with key as id
  public void deleteProductWithInventory(UUID productId) {

    productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    //1. Call the GET /inventory/product/{productId} endpoint to get the inventory ID
    String getInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/internal/product/" + productId;
    //String getInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/internal/product/" + productId;
    webClientBuilder.build()
            .get()
            .uri(getInventoryUrl)
            .retrieve()
            .bodyToMono(Result.class)
            .flatMap(result -> {
              if(result != null && result.isFlag() && result.getData() != null) {
                var objectMapper = new ObjectMapper();
                var inventoryDto = objectMapper.convertValue(result.getData(), InventoryDto.class);
                var inventoryId = inventoryDto.inventoryId();

                //2. Call DELETE /inventory/{inventoryId} endpoint to delete the inventory
                String deleteInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/" + inventoryId;
                //String deleteInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/inventory/" + inventoryId;
                return webClientBuilder.build()
                        .delete()
                        .uri(deleteInventoryUrl)
                        .retrieve()
                        .bodyToMono(Result.class);
              } else {
                // Handle the case where the inventory is not found
                logger.warn("******Inventory not found for productId: {}*******", productId);
                return Mono.empty();
              }

            }).doOnError(error -> logger.error("Error occurred while deleting inventory for product {}: {}", productId, error.getMessage()))
            .doOnSuccess(response -> {
                      if (response != null && response.isFlag()) {
                        logger.info("*******Inventory deleted successfully for product {}*******", productId);
                      } else {
                        logger.warn("*******Inventory deletion returned a failure response for product {}: {}*******",
                                productId, response != null ? response.getMessage() : "No message");
                      }
                    })
            .doFinally(signal -> {
              // whatever happens, delete the product from the database
              productRepository.deleteById(productId);
              logger.info("*******Product {} deleted successfully *******", productId);
            })
            .subscribe();
  }

  //TODO: Change to return Mono<Void> to align with reactive programming practices and handle errors properly:
  @Override
  @CacheEvict(value = "product", key = "#productId") //delete cache with key as id
  public void deleteProductWithInventoryUsingExternalizedService(UUID productId) {
    productRepository.findById(productId).
            orElseThrow(() -> new ProductNotFoundException(productId));
    //1. Call the GET /inventory/internal/product/{productId} endpoint to get the inventory ID
    inventoryServiceClient.getInventoryByProductId(productId)
            .flatMap(inventoryDto -> {
              var inventoryId = inventoryDto.inventoryId();
              //2. Call DELETE /inventory/{inventoryId} endpoint to delete the inventory
              return inventoryServiceClient.deleteInventory(inventoryId);

            })
            .doOnError(error ->
                    logger.error("Error happens while deleting inventory for product {}: {}", productId, error.getMessage()))
            .doOnSuccess(response -> {
              if (response != null && response.isFlag()) {
                logger.info("*******Inventory delete is successfully for product {}*******", productId);
              } else {
                logger.warn("*******Inventory delete returned a failure response for product {}: {}*******",
                        productId, response != null ? response.getMessage() : "No message");
              }
            })
    .doFinally(signal -> {;
              // whatever happens, delete the product from the database
              productRepository.deleteById(productId);
              logger.info("*******Product {} delete is successful *******", productId);
            })
            .subscribe();
  }


  @Override
  public void bulkDeleteProductsWithInventories(List<UUID> productIds) {
    var products = productRepository.findAllById(productIds);

    //1. extract all found product ids and collect them into a list
    var foundProductIds = products.stream()
            .map(Product::getProductId)
            .toList();

    //2.determine missing ids and collect them into a list
    var missingIds = productIds.stream()
            .filter(id -> !foundProductIds.contains(id))
            .toList();

    //3.throw exception if there are missing ids
    if (!missingIds.isEmpty()) {
      throw new ProductNotFoundException(missingIds);
    }

    //4.proceed to delete only found inventories and products
    for(Product product: products){
      var productId = product.getProductId();

      //Step1. Call the GET /inventory/product/{productId} endpoint to get the inventory ID
      var getInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/internal/product/" + productId;
      //var getInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/internal/product/" + productId;

      webClientBuilder.build()
              .get()
              .uri(getInventoryUrl)
              .retrieve()
              .bodyToMono(Result.class)
              .flatMap(result -> {
                if(result != null && result.isFlag() && result.getData() != null) {
                  var objectMapper = new ObjectMapper();
                  var inventoryDto = objectMapper.convertValue(result.getData(), InventoryDto.class);
                  var inventoryId = inventoryDto.inventoryId();

                  //Step2. Call DELETE /inventory/{inventoryId} endpoint to delete the inventory
                  var deleteInventoryUrl = "http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/" + inventoryId;
                  //var deleteInventoryUrl = inventoryServiceConfig.getBaseUrl() + "/inventory/" + inventoryId;
                  return webClientBuilder.build()
                          .delete()
                          .uri(deleteInventoryUrl)
                          .retrieve()
                          .bodyToMono(Result.class);

                } else {
                  // Handle the case where the inventory is not found
                  logger.warn("*****Inventory not found for productId: {}*******", productId);
                  return Mono.empty();
                }
              })
              .doOnError(error -> logger.error("Error occurred while deleting inventory for the product {}: {}", productId, error.getMessage()))
              .doOnSuccess(response -> {
                if (response != null && response.isFlag()) {
                  logger.info("******Inventory deleted successfully for product {}*******", productId);
                } else {
                  logger.warn("******Failed to delete inventory for product {}: {}*******",
                          productId, response != null ? response.getMessage() : "No message");
                }
              })
              .doFinally(signal -> {
                // whatever happens, delete the product from the database
                productRepository.deleteById(productId);
                logger.info("******Product {} deleted successfully *******", productId);
              })
              .subscribe();
    }

  }

  /**
   * @param productIds
   */
  @Override
  //TODO: Delete from cache immediately after deleting products
  public void bulkDeleteProductsWithInventoriesUsingExternalizedService(List<UUID> productIds) {
    var products = productRepository.findAllById(productIds);
    //1. extract all found product ids and collect them into a list
    var foundProductIds = products.stream()
            .map(Product::getProductId)
            .toList();
    //2.determine missing ids and collect them into a list
    var missingIds = productIds.stream()
            .filter(id -> !foundProductIds.contains(id))
            .toList();
    //3.throw exception if there are missing ids
    if (!missingIds.isEmpty()) {
      throw new ProductNotFoundException(missingIds);
    }
    //4.proceed to delete only found inventories and products
    for(Product product: products){
      //Step1. Call the GET /inventory/internal/product/{productId} endpoint to get the inventory ID
      inventoryServiceClient.getInventoryByProductId(product.getProductId())
              .flatMap(inventoryDto -> {
                var inventoryId = inventoryDto.inventoryId();
                //Step2. Call DELETE /inventory/{inventoryId} endpoint to delete the inventory
                return inventoryServiceClient.deleteInventory(inventoryId);
              })
              .doOnError(error ->
                      logger.error("Error  deleting inventory for product {}: {}", product.getProductId(), error.getMessage()))
              .doOnSuccess(response -> {
                if (response != null && response.isFlag()) {
                  logger.info("******Inventory delete successfully for product {}*******", product.getProductId());
                } else {
                  logger.warn("******Fail to delete inventory for product {}: {}*******",
                          product.getProductId(), response != null ? response.getMessage() : "No message");
                }
              })
              .doFinally(signal -> {
                // whatever happens, delete the products from the database
                productRepository.deleteById(product.getProductId());
                logger.info("******Product {} delete successfully *******", product.getProductId());
              }).subscribe();
    }

  }

  ///method to ensure productQuantity and availableStock are in sync
  private void ensureProductQuantityAndAvailableStockAreInSync(Product product) {
    if (product.getProductQuantity() != null && product.getAvailableStock() == null) {
      product.setAvailableStock(product.getProductQuantity());
    } else if (product.getAvailableStock() != null && product.getProductQuantity() == null) {
      product.setProductQuantity(product.getAvailableStock());
    } else if (product.getProductQuantity() != null && product.getAvailableStock() != null
            && !product.getProductQuantity().equals(product.getAvailableStock())) {
      product.setAvailableStock(product.getProductQuantity());
    }
  }


}
