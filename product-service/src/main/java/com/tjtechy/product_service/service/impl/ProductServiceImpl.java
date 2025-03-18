/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.service.impl;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.repository.ProductRepository;
import com.tjtechy.product_service.service.ProductService;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * This class is the implementation of the ProductService interface.
 */
@Service //create a bean of this class
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;

  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
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
     * E.g log.debug, log.error, log.warn etc.
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

    @CachePut(value = "product", key = "#product.productId") //store in "products" cache with key as id
    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
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
        foundProduct.setProductQuantity(product.getProductQuantity());
        foundProduct.setAvailableStock(product.getAvailableStock());
        logger.info("*******product {} updated successfully *******", productId);
        return productRepository.save(foundProduct);
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

  //TODO: LOGIC TO CLEAR CACHE FOR A SPECIFIC PRODUCT. LOGIC MUST BE IMPLEMENTED IN THE INTERFACE


}
