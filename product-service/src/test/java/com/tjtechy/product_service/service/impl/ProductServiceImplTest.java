/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.service.impl;

import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.repository.ProductRepository;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  /**
   * We are not mocking the logger here, because we are not testing the logging behavior.
   * The test is focused on the service methods.
   */
  private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplTest.class);

  @Mock
  private ProductRepository productRepository;


  @InjectMocks
  private ProductServiceImpl productService;

  private List<Product> productList;



  @BeforeEach
  void setUp() {
    // Create a list of products
    productList = Arrays.asList(
            new Product( UUID.randomUUID(),
                    "Product 1",
                    "product 1 description",
                    new BigDecimal("100.0"),
                    100,
                    "Category 1",
                    10,
                    LocalDate.of(2026, 10, 10),
                    LocalDate.of(2021, 9, 5),
                    LocalDate.of(2021, 9, 5)),
            new Product( UUID.randomUUID(),
                    "Product 2",
                    "product 2 description",
                    new BigDecimal("200.0"),
                    200,
                    "Category 2",
                    20,
                    LocalDate.of(2026, 10, 10),
                    LocalDate.of(2021, 9, 5),
                    LocalDate.of(2021, 9, 5))
    );
  }

  @AfterEach
  void tearDown() {
  }

  /**
   * Test the getAllProducts method.
   * The method should return a list of products.
   */
  @Test
  void getAllProductsSuccess() {
    // Given
    given(productRepository.findAll()).willReturn(productList);

    // When
    List<Product> products = productService.getAllProducts();

    // Then
    assertNotNull(products);
    assertEquals(2, products.size());
    assertEquals("Product 1", products.get(0).getProductName());
    assertEquals("Product 2", products.get(1).getProductName());

    verify(productRepository, times(1)).findAll();
    logger.info("*******Fetched {} products*******", products.size());
  }

  /**
   * Test the getProductById method.
   * The method should return a product with the specified ID.
   */
  @Test
  void getProductByIdSuccess() {
    // Given
    UUID productId = productList.get(0).getProductId();
    given(productRepository.findById(productId)).willReturn(Optional.of(productList.get(0)));

    // When
    Product product = productService.getProductById(productId);

    // Then
    assertNotNull(product);
    assertEquals("Product 1", product.getProductName());
    assertEquals("product 1 description", product.getProductDescription());
    assertEquals(new BigDecimal("100.0"), product.getProductPrice());
    assertEquals(100, product.getProductQuantity());
    assertEquals("Category 1", product.getProductCategory());
    assertEquals(10, product.getAvailableStock());
    assertEquals(LocalDate.of(2026, 10, 10), product.getExpiryDate());
    assertEquals(LocalDate.of(2021, 9, 5), product.getManufacturedDate());
    assertEquals(LocalDate.of(2021, 9, 5), product.getUpdatedAt());

    verify(productRepository, times(1)).findById(productId);
  }

  /**
   * Test the getProductById method when the product is not found.
   * The method should throw a ProductNotFoundException.
   */
  @Test
  void getProductByIdNotFound() {
    // Given
    UUID productId = UUID.randomUUID();
    given(productRepository.findById(productId)).willReturn(Optional.empty());

    // When
    Exception exception = assertThrows(RuntimeException.class, () -> productService.getProductById(productId));

    // Then
    assertNotNull(exception);
    assertEquals(ProductNotFoundException.class, exception.getClass());
  }

  /**
   * Test the saveProduct method.
   * The method should save a product and return the saved product.
   */
  @Test
  void saveProduct() {
    // Given
    Product product = new Product( UUID.randomUUID(),
            "Product 3",
            "product 3 description",
            new BigDecimal("300.0"),
            300,
            "Category 3",
            30,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    given(productRepository.save(product)).willReturn(product);

    // When
    Product savedProduct = productService.saveProduct(product);

    // Then
    assertNotNull(savedProduct);
    assertEquals("Product 3", savedProduct.getProductName());
    assertEquals("product 3 description", savedProduct.getProductDescription());
    assertEquals(new BigDecimal("300.0"), savedProduct.getProductPrice());
    assertEquals(300, savedProduct.getProductQuantity());
    assertEquals("Category 3", savedProduct.getProductCategory());
    assertEquals(30, savedProduct.getAvailableStock());
    assertEquals(LocalDate.of(2026, 10, 10), savedProduct.getExpiryDate());
    assertEquals(LocalDate.of(2021, 9, 5), savedProduct.getManufacturedDate());
    assertEquals(LocalDate.of(2021, 9, 5), savedProduct.getUpdatedAt());
    verify(productRepository, times(1)).save(product);
  }

  /**
   * Test the updateProduct method.
   * The method should update a product and return the updated product.
   */
  @Test
  void updateProductSuccess() {
    // Given
    UUID productId = productList.get(0).getProductId();

    var updatedProduct = new Product( UUID.randomUUID(),
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    given(productRepository.findById(productId)).willReturn(Optional.of(productList.get(0)));
    given(productRepository.save(productList.get(0))).willReturn(productList.get(0));

    // When
    Product updated = productService.updateProduct(productId, updatedProduct);

    // Then
    assertNotNull(updated);
    assertEquals("Product 1 updated", updated.getProductName());
    assertEquals("product 1 description updated", updated.getProductDescription());
    assertEquals(new BigDecimal("100.0"), updated.getProductPrice());
    assertEquals(100, updated.getProductQuantity());
    assertEquals("Category 1", updated.getProductCategory());
    assertEquals(10, updated.getAvailableStock());
    assertEquals(LocalDate.of(2026, 10, 10), updated.getExpiryDate());
    assertEquals(LocalDate.of(2021, 9, 5), updated.getManufacturedDate());
    assertEquals(LocalDate.of(2021, 9, 5), updated.getUpdatedAt());
    verify(productRepository, times(1)).findById(productId);
  }

  /**
   * Test deleteProduct method.
   */
  @Test
  void deleteProductSuccess() {
    // Given
    UUID productId = productList.get(0).getProductId();
    given(productRepository.findById(productId)).willReturn(Optional.of(productList.get(0)));

    // When
    productService.deleteProduct(productId);

    // Then
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).deleteById(productId);
  }


  /**
   * Test the clearAllCache method.
   */
  @Test
  void clearAllCache() {
    // When
    productService.clearAllCache();

    // Then
    verify(productRepository, times(0)).findAll(); //the repository method is not called
    logger.info("*******Cleared all cache*******");
  }
}