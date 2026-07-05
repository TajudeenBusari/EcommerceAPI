/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.service.impl;

import com.tjtechy.*;
import com.tjtechy.client.InventoryServiceClient;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import com.tjtechy.product_service.config.InventoryServiceConfig;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  private static final Logger logger = LoggerFactory.getLogger(ProductServiceImplTest.class);

  @Mock
  private ProductRepository productRepository;

  @Mock
  private InventoryServiceConfig inventoryServiceConfig;

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private InventoryServiceClient inventoryServiceClient;

  @InjectMocks
  private ProductServiceImpl productService;

  private List<Product> products;

  @BeforeEach
  void setUp() {
    // Create a list of products
    products = Arrays.asList(
            new Product( UUID.randomUUID(),
                    "Product 1",
                    "product 1 description",
                    new BigDecimal("100.0"),
                    100,
                    "Category 1",
                    10,
                    LocalDate.now(), //manufactured date
                    LocalDate.now().plusYears(5), //expiry date
                    LocalDate.now()), //updated date
            new Product( UUID.randomUUID(),
                    "Product 2",
                    "product 2 description",
                    new BigDecimal("200.0"),
                    200,
                    "Category 2",
                    20,
                    LocalDate.now(), //manufactured date
                    LocalDate.now().plusYears(5), //expiry date
                    LocalDate.now()) //updated date
    );
  }

  @AfterEach
  void tearDown() {}



  @Test
  @DisplayName("Test for getAllProducts method (GET /products)")
  void getAllProductsSuccess() {
    //Given
    given(productRepository.findAll()).willReturn(products);

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

  @Test
  @DisplayName("Test for getProductById method")
  void getProductByIdSuccess() {

    // Given
    UUID productId = products.getFirst().getProductId();
    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));

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
    assertNotNull(product.getExpiryDate());
    assertNotNull(product.getManufacturedDate());
    assertNotNull(product.getUpdatedAt());
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  @DisplayName("Test for getProductById method when product is not found.")
  void testGetProductByIdNotFound() {
    // Given
    UUID productId = UUID.randomUUID();
    given(productRepository.findById(productId)).willReturn(Optional.empty());

    // When
    Exception exception = assertThrows(RuntimeException.class, () -> productService.getProductById(productId));

    // Then
    assertNotNull(exception);
    assertEquals(ProductNotFoundException.class, exception.getClass());
  }

  @Test
  @DisplayName("Test for saveProduct method Success")
  void saveProductSuccess() {
    // Given
    Product product = new Product( UUID.randomUUID(),
            "Product 3",
            "product 3 description",
            new BigDecimal("300.0"),
            300,
            "Category 3",
            30,
            LocalDate.now(), //manufactured date
            LocalDate.now().plusYears(5), //expiry date
            LocalDate.now()); //updated date

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
    assertEquals(300, savedProduct.getAvailableStock()); //automatically sync with productQuantity
    assertNotNull(savedProduct.getExpiryDate());
    assertNotNull(savedProduct.getManufacturedDate());
    assertNotNull(savedProduct.getUpdatedAt());
    verify(productRepository, times(1)).save(product);
  }

  @Test
  @DisplayName("Test for saveProductWithInventory method Success.")
  void saveProductWithInventorySuccess() {
    //deprecated
  }

  @Test
  @DisplayName("Test for saveProduct method using externalized service")
  void saveProductWithInventoryUsingExternalizedService() {
    // Given
    var product = products.getFirst();

    given(productRepository.save(any(Product.class))).willReturn(product);

    //mock the inventory service client
    doNothing().when(inventoryServiceClient).createInventoryForProductAsync(any(CreateInventoryDto.class));


    // When
    Product savedProduct = productService.saveProductWithInventoryUsingExternalizedService(product);

    // Then
    assertNotNull(savedProduct);
    verify(productRepository, times(1)).save(product);
    assertEquals(product.getProductId(), savedProduct.getProductId());
  }

  @Test
  @DisplayName("Test for saveProductWithInventoryUsingExternalizedService method when inventory service is down")
  void testSaveProductWithInventoryUsingExternalizedServiceWhenInventoryServiceIsDown() {
    // Given
    var product = products.getFirst();

    given(productRepository.save(any(Product.class))).willReturn(product);

    //mock the inventory service client to throw an exception
    doThrow(new RuntimeException("Inventory service is down")).when(inventoryServiceClient).createInventoryForProductAsync(any(CreateInventoryDto.class));

    // When
    Exception exception = assertThrows(RuntimeException.class, () -> productService.saveProductWithInventoryUsingExternalizedService(product));

    // Then
    assertNotNull(exception);
    assertEquals("Inventory service is down", exception.getMessage());
    verify(productRepository, times(1)).save(product);
  }

  @Test
  @DisplayName("Test for saveProductWithInventory method when inventory service is down.")
  void testSaveProductWithInventoryServiceThrowError(){
    //Given
    Product product = new Product(
            UUID.randomUUID(),
            "Product 4",
            "product 4 description",
            new BigDecimal("400.0"),
            400,
            "Category 4",
            40,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    InventoryDto inventoryDto = new InventoryDto(
            null,
            product.getProductId(),
            product.getProductQuantity(),
            product.getAvailableStock()
    );
    given(productRepository.save(any(Product.class))).willReturn(product);

    //mock the inventory service config
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    //mock webclient chain
    WebClient webClient = mock(WebClient.class);
    WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
    WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    //
    doReturn(requestBodyUriSpec).when(webClient).post();
    doReturn(requestBodySpec).when(requestBodyUriSpec).uri("http://inventory-service/api/v1/inventory/internal/create");
    doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any(CreateInventoryDto.class));
    doReturn(responseSpec).when(requestBodySpec).retrieve();

    when(responseSpec.bodyToMono(Result.class)).thenThrow(new RuntimeException("Inventory service is down"));

    //mock the webclient builder
    when(webClientBuilder.build()).thenReturn(webClient);

    //When
    //Note: saveProductWithInventory is deprecated
    Exception exception = assertThrows(RuntimeException.class, () -> productService.saveProductWithInventory(product));

    //Then
    assertNotNull(exception);
    verify(productRepository, times(1)).save(product);
    verify(webClient, times(1)).post();
    assertEquals("Inventory service is down", exception.getMessage());
  }


  @Test
  @DisplayName("Test for updateProduct method Success")
  void updateProductSuccess() {
    // Given
    UUID productId = products.getFirst().getProductId();

    var updatedProduct = new Product( productId,
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));
    given(productRepository.save(products.getFirst())).willReturn(products.getFirst());

    // When
    Product updated = productService.updateProduct(productId, updatedProduct);

    // Then
    assertNotNull(updated);
    assertEquals("Product 1 updated", updated.getProductName());
    assertEquals("product 1 description updated", updated.getProductDescription());
    assertEquals(new BigDecimal("100.0"), updated.getProductPrice());
    assertEquals(100, updated.getProductQuantity());
    assertEquals("Category 1", updated.getProductCategory());
    assertEquals(100, updated.getAvailableStock());
    assertNotNull(updated.getExpiryDate());
    assertNotNull(updated.getManufacturedDate());
    assertNotNull(updated.getUpdatedAt());
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void updateProductWithInventorySuccess() {
    //Given
    var productId = products.getFirst().getProductId();
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

    InventoryDto inventoryDto = new InventoryDto(
            null,
            productId,
            1,
            10
    );
    Result getInventoryResult = new Result("Inventory retrieved successfully", true, inventoryDto, StatusCode.SUCCESS);
    Result updateInventoryResult = new Result("Inventory updated successfully", true, inventoryDto, StatusCode.SUCCESS);

    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));
    given(productRepository.save(products.getFirst())).willReturn(updatedProduct);

    //mock the inventory service config
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");


    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersSpec<?> getRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

    WebClient.RequestBodyUriSpec putRequestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    WebClient.RequestBodySpec putRequestBodySpec = mock(WebClient.RequestBodySpec.class);
    WebClient.ResponseSpec putResponseSpec = mock(WebClient.ResponseSpec.class);


    // Mock the WebClient builder
    when(webClientBuilder.build()).thenReturn(webClient);
    //OR doReturn(webClient).when(webClientBuilder).build();

    // Mock WebClient behavior for GET /inventory/internal/product/{productId}
    doReturn(getRequestHeadersUriSpec).when(webClient).get();
    doReturn(getRequestHeadersSpec).when(getRequestHeadersUriSpec).uri("http://inventory-service/api/v1/inventory/internal/product/" + productId);
    when(getRequestHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    when(getResponseSpec.bodyToMono(Result.class)).thenReturn(Mono.just(getInventoryResult));


    // Mock WebClient behavior for PUT /inventory/internal/update/{inventoryId}
    doReturn(putRequestBodyUriSpec).when(webClient).put();
    doReturn(putRequestBodySpec).when(putRequestBodyUriSpec).uri("http://inventory-service/api/v1/inventory/internal/update/" + inventoryDto.inventoryId());
    doReturn(putRequestBodySpec).when(putRequestBodySpec).contentType(MediaType.APPLICATION_JSON);
    doReturn(putRequestBodySpec).when(putRequestBodySpec).bodyValue(any(UpdateInventoryDto.class));
    doReturn(putResponseSpec).when(putRequestBodySpec).retrieve();
    when(putResponseSpec.bodyToMono(Result.class)).thenReturn(Mono.just(updateInventoryResult));


    //When
    var result = productService.updateProductWithInventory(productId, updatedProduct);
    //Then
    assertNotNull(result);
    assertEquals("Product 1 updated", result.getProductName());
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).save(products.getFirst());
    verify(webClientBuilder, times(2)).build(); //GET and PUT
  }

  @Test
  @DisplayName("Test for updateProductWithInventoryUsingExternalizedService method")
  void updateProductWithInventoryUsingExternalizedServiceSuccess() {
    //Given
    var product = products.getFirst();
    var productId = products.getFirst().getProductId();
    var updatedProduct = new Product( productId,
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.now(), //manufactured date;
            LocalDate.now().plusYears(5), //expiry date
            LocalDate.now()); //updated date

    given(productRepository.findById(productId)).willReturn(Optional.of(product));
    given(productRepository.save(product)).willReturn(updatedProduct);

    //mock the get inventory by product id
    InventoryDto inventoryDto = new InventoryDto(
            100L,
            productId,
            1,
            10
    );
    when(inventoryServiceClient.getInventoryByProductId(productId)).thenReturn(Mono.just(inventoryDto));

    //mocks the inventory service client to update the inventory for the product
    when(inventoryServiceClient.updateInventory(eq(inventoryDto.inventoryId()), any(UpdateInventoryDto.class)))
            .thenReturn(Mono.just(new Result("Inventory updated successfully", true, inventoryDto, StatusCode.SUCCESS)));

    //When
    var result = productService.updateProductWithInventoryUsingExternalizedService(productId, updatedProduct);

    //Then
    assertNotNull(result);
    assertEquals("Product 1 updated", result.getProductName());
  }

  @Test
  @DisplayName("Test for updateProductWithInventory method when inventory is not found")
  void testUpdateProductWithInventoryNotFound(){
    // Given
    var productId = products.getFirst().getProductId();
    var updatedProduct = new Product( UUID.randomUUID(),
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.now(),
            LocalDate.now().plusYears(5),
            LocalDate.now()
           );
    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));
    given(productRepository.save(products.getFirst())).willReturn(updatedProduct);

    //mock inventory service to return an error when trying to retrieve inventory
    InventoryDto inventoryDto = new InventoryDto(
            100L,
            productId,
            1,
            100
    );

    //1. mock the inventory service getInventoryByProductId method to throw an error
    when(inventoryServiceClient.getInventoryByProductId(productId)).thenReturn(Mono.error(new IllegalArgumentException("Failed to retrieve inventory for productId: " + productId)));
    //2. No need to mock the updateInventory method as it won't be called in this case.

    //When
    var result = productService.updateProductWithInventoryUsingExternalizedService(productId, updatedProduct); //product gets updated even though inventory not found

    //Then
    assertNotNull(result);
  }

  @Test
  @DisplayName("Test for updateProductWithInventory method when inventory is not found")
  void updateProductWithInventoryNotFound() {
    // Given
    var productId = products.getFirst().getProductId();

    Product updatedProduct = new Product( UUID.randomUUID(),
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    InventoryDto inventoryDto = new InventoryDto(
            null,
            productId,
            1,
            10
    );


    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));
    given(productRepository.save(products.getFirst())).willReturn(updatedProduct);

    //mock the inventory service config
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersSpec<?> getRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

    // Mock the WebClient builder
    when(webClientBuilder.build()).thenReturn(webClient);

    // Mock WebClient behavior for GET /inventory/internal/product/{productId}
    doReturn(getRequestHeadersUriSpec).when(webClient).get();
    doReturn(getRequestHeadersSpec).when(getRequestHeadersUriSpec).uri("http://inventory-service/api/v1/inventory/internal/product/" + productId);
    when(getRequestHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    when(getResponseSpec.bodyToMono(Result.class)).thenReturn(Mono.error(new RuntimeException("Inventory not found for productId" + productId)));


    //When
    var result = productService.updateProductWithInventory(productId, updatedProduct);

    //Then
    assertNotNull(result);
    verify(webClientBuilder, times(1)).build(); //only GET
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).save(products.getFirst());
  }

  @Test
  @DisplayName("Test for deleteProduct method Success")
  void deleteProductSuccess() {
      // Given
      UUID productId = products.getFirst().getProductId();
      given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));

      // When
      productService.deleteProduct(productId);

      // Then
      verify(productRepository, times(1)).findById(productId);
      verify(productRepository, times(1)).deleteById(productId);
  }

  @Test
  @DisplayName("Test for clearAllCache method")
  void clearAllCache() {
    // When
    productService.clearAllCache();

    // Then
    verify(productRepository, times(0)).findAll(); //the repository method is not called
    logger.info("*******Cleared all cache*******");
  }

  @Test
  @DisplayName("Test for bulkDeleteProducts method Success when some products is/are missing")
  void testBulkDeleteProductsWhenSomeProductIsMissingSuccess() {
    // Given
    var missingId = UUID.randomUUID();
    List<UUID> productIds = Arrays.asList(products.getFirst().getProductId(), products.get(1).getProductId(), missingId);
    var foundProducts = Arrays.asList(products.get(0), products.get(1));

    // Mock the product repository to return the found products
    given(productRepository.findAllById(productIds)).willReturn(foundProducts);

    // When
    /*
     * The bulkDeleteProducts method is called with the list of product IDs.
     * It should delete the found products and throw an exception for the missing product.
     */
    var exception = assertThrows(ProductNotFoundException.class, () -> productService.bulkDeleteProducts(productIds));

    // Then
    assertTrue(exception.getMessage().contains("Product not found with ids:")); //contains should just state part of the exception message
    verify(productRepository, times(1)).deleteAll(products);
    verify(productRepository).deleteAll(foundProducts);
  }

  @Test
  @DisplayName("Test for bulkDeleteProducts method when all products are missing")
  void testBulkDeleteProductsWhenAllProductIsMissingSuccess() {
    // Given
    var missingId1 = UUID.randomUUID();
    var missingId2 = UUID.randomUUID();
    List<UUID> productIds = Arrays.asList(missingId1, missingId2);

    // Mock the product repository to return an empty list
    given(productRepository.findAllById(productIds)).willReturn(Collections.emptyList());

    // When
    var exception = assertThrows(ProductNotFoundException.class, () -> productService.bulkDeleteProducts(productIds));

    // Then
    assertTrue(exception.getMessage().contains("Product not found with ids:"));
    verify(productRepository, times(0)).deleteAll(anyList());
  }

  @Test
  @DisplayName("Test for bulkDeleteProducts method Success when no product is missing")
  void testBulkDeleteProductsWhenNoProductIsMissingSuccess() {
    // Given
    List<UUID> productIds = Arrays.asList(products.get(0).getProductId(), products.get(1).getProductId());

    // Mock the product repository to return the found products
    given(productRepository.findAllById(productIds)).willReturn(products);

    // When
    productService.bulkDeleteProducts(productIds);

    // Then
    verify(productRepository, times(1)).deleteAll(products);
  }

  @Test
  @DisplayName("Test for deleteProductWithInventory method Success")
  void testDeleteProductWithInventorySuccess(){
    //Given
    var productId = products.getFirst().getProductId();

    InventoryDto inventoryDto = new InventoryDto(
            null,
            productId,
            1,
            10
    );
    Result getInventoryResult = new Result("Inventory retrieved successfully", true, inventoryDto, StatusCode.SUCCESS);
    Result deleteInventoryResult = new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS);

    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));

    //mock the inventory service config
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    WebClient webClient = mock(WebClient.class);

    WebClient.RequestHeadersSpec<?> getRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

    WebClient.RequestHeadersUriSpec<?> deleteRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> deleteRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec deleteResponseSpec = mock(WebClient.ResponseSpec.class);

    // Mock the WebClient builder
    when(webClientBuilder.build()).thenReturn(webClient);

    // Mock WebClient behavior for GET /inventory/internal/product/{productId}
    doReturn(getRequestHeadersUriSpec).when(webClient).get();
    doReturn(getRequestHeadersSpec).when(getRequestHeadersUriSpec).uri("http://inventory-service/api/v1/inventory/internal/product/" + productId);
    when(getRequestHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    when(getResponseSpec.bodyToMono(Result.class)).thenReturn(Mono.just(getInventoryResult));

    // Mock WebClient behavior for DELETE /inventory/{inventoryId}
    doReturn(deleteRequestHeadersUriSpec).when(webClient).delete();
    doReturn(deleteRequestHeadersSpec).when(deleteRequestHeadersUriSpec).uri("http://inventory-service/api/v1/inventory/" + inventoryDto.inventoryId());
    when(deleteRequestHeadersSpec.retrieve()).thenReturn(deleteResponseSpec);
    when(deleteResponseSpec.bodyToMono(Result.class)).thenReturn(Mono.just(deleteInventoryResult));

    //When
    productService.deleteProductWithInventory(productId);

    //then
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).deleteById(productId);
    //very GET And DELETE URLs were called
    verify(webClient).get();
    verify(webClient).delete();
  }

  @Test
  @DisplayName("Test for deleteProductWithInventoryExternalized method Success")
  void testDeleteProductWithInventoryExternalizedSuccess(){
    //Given
    var productId = products.getFirst().getProductId();

    InventoryDto inventoryDto = new InventoryDto(
            null,
            productId,
            1,
            10
    );
    //mock the product repository to return an existing product
    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));

    //mock the inventory service client to return an inventory by product id
    when(inventoryServiceClient.getInventoryByProductId(productId)).thenReturn(Mono.just(inventoryDto));

    //mock the inventory service client to delete the inventory by inventory id
    when(inventoryServiceClient.deleteInventory(inventoryDto.inventoryId()))
            .thenReturn(Mono.just(
                    new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS)));
    //When
    productService.deleteProductWithInventoryUsingExternalizedService(productId);

    //Then
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).deleteById(productId);
  }

  @Test
  @DisplayName("Test for bulkDeleteProductSWithInventoryExternalized method Success")
  void testBulkDeleteProductsWithInventoryExternalizedSuccess() {
    // Given
    List<UUID> productIds = Arrays.asList(products.get(0).getProductId(), products.get(1).getProductId());

    // Mock the product repository to return the found products
    given(productRepository.findAllById(productIds)).willReturn(products);

    // Mock the inventory service client to delete the inventory for each product
    for (Product product : products) {
      InventoryDto inventoryDto = new InventoryDto(null, product.getProductId(), 1, 10);
      when(inventoryServiceClient.getInventoryByProductId(product.getProductId())).thenReturn(Mono.just(inventoryDto));
      when(inventoryServiceClient.deleteInventory(inventoryDto.inventoryId()))
              .thenReturn(Mono.just(new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS)));
    }

    // When
    productService.bulkDeleteProductsWithInventoriesUsingExternalizedService(productIds);

    // Then
    verify(productRepository, times(1)).findAllById(productIds);
  }

  @Test
  @DisplayName("Test for deleteProductWithInventory method when inventory is not found")
  void testDeleteProductWithInventoryNotFound() {
    // Given
    var productId = products.getFirst().getProductId();

    // Mock the product repository to return an existing product
    given(productRepository.findById(productId)).willReturn(Optional.of(products.getFirst()));

    // Mock the inventory service config
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersSpec<?> getRequestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

    // Mock the WebClient builder
    when(webClientBuilder.build()).thenReturn(webClient);

    // Mock WebClient behavior for GET /inventory/internal/product/{productId}
    doReturn(getRequestHeadersUriSpec).when(webClient).get();
    doReturn(getRequestHeadersSpec).when(getRequestHeadersUriSpec).uri("http://inventory-service/api/v1/inventory/internal/product/" + productId);
    when(getRequestHeadersSpec.retrieve()).thenReturn(getResponseSpec);
    when(getResponseSpec.bodyToMono(Result.class)).thenThrow(new RuntimeException("Inventory not found for productId" + productId)); //warning to be specific

    // When
    var exception = assertThrows(RuntimeException.class, () -> productService.deleteProductWithInventory(productId));

    // Then
    assertNotNull(exception);
  }

  @Test
  @DisplayName("Test for deleteProductWithInventory method when product is not found")
  void testDeleteProductWithInventoryWithProductNotFound(){
    //Given
    var productId = UUID.randomUUID();

    //Mock the product repository to return an empty optional
    given(productRepository.findById(productId)).willReturn(Optional.empty());

    //When
    var exception = assertThrows(ProductNotFoundException.class, () -> productService.deleteProductWithInventory(productId));

    //Then
    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Product not found with id:"));

    //verify product lookup was attempted
    verify(productRepository).findById(productId);

    //Ensure no further interaction
    verifyNoMoreInteractions(webClientBuilder);
    verify(productRepository, never()).deleteById(productId);
  }

}