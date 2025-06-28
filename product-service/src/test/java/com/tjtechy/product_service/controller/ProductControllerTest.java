/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;
import com.tjtechy.product_service.exception.ExceptionHandlingAdvice;
import com.tjtechy.product_service.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cache.type=none",
        "spring.redis.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
/**
 * So @ContextConfiguration(classes = {...}) forces Spring to load only what you need,
 * overriding the default component scan or @SpringBootApplication class
 * In @WebMvcTest, adding @ContextConfiguration(classes = {ProductController.class}) helps you:
 * Explicitly isolate the controller
 * Prevent JPA and Redis config from being loaded
 * Speed up your tests and avoid irrelevant bean creation errors
 */
@ContextConfiguration(classes = {ProductController.class})
@Import(ExceptionHandlingAdvice.class)//Import the ExceptionHandlingAdvice class to handle exceptions in the controller tests
class ProductControllerTest {

  @MockitoBean
  private ProductService productService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  List<Product> productList;

  @BeforeEach
  void setUp() {
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
   * Test for {@link ProductController#getAllProducts()} GET /api/v1/product success
   * <p> Verifies that the getAllProducts method returns a List of products. </p>
   *
   */
  @Test
  @DisplayName("Test for getAllProducts() GET /api/v1/product success")
  void testGetAllProductsSuccess() throws Exception {
    //given
    given(productService.getAllProducts()).willReturn(productList);

    //when and then
    mockMvc.perform(get(baseUrl + "/product")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Get All Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].productId").isNotEmpty())
            .andExpect(jsonPath("$.data[0].productName").value("Product 1"))
            .andExpect(jsonPath("$.data[0].productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data[0].productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data[0].productQuantity").value(100))
            .andExpect(jsonPath("$.data[0].availableStock").value(10))
            .andExpect(jsonPath("$.data[0].expiryDate").value("2026-10-10"));
  }

  /**
   * Test for {@link ProductController#getProductById(UUID)} GET /api/v1/product/{productId} success
   * <p> Verifies that the getProductById method returns a product. </p>
   */
  @Test
  @DisplayName("Test for getProductById() GET /api/v1/product/{productId} success")
  void testGetProductByIdSuccess() throws Exception {
    //given
    UUID productId = productList.get(0).getProductId();
    given(productService.getProductById(productId)).willReturn(productList.get(0));

    //when and then
    mockMvc.perform(get(baseUrl + "/product/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Get One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1"))
            .andExpect(jsonPath("$.data.productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data.productQuantity").value(100))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-10-10"));
  }

  /**
   * Test for {@link ProductController#addProduct(CreateProductDto)} POST /api/v1/product success
   * <p> Verifies that the addProduct method returns a product. </p>
   */
  @Test
  @DisplayName("Test for addProduct() POST /api/v1/product success")
  void testAddProductSuccess() throws Exception {
    //given
    var createRequest = new CreateProductDto(
            "Product 1",
            "product 1 description",
            "Category 1",
            100,
            10,
            new BigDecimal("100.0"),
            LocalDate.of(2024, 10, 10), //manufactured date
            LocalDate.of(2026, 9, 5)); //expiry date


    var json = objectMapper.writeValueAsString(createRequest);

    //mock the saveProduct method of the service class
    given(productService.saveProduct(any(Product.class))).willReturn(new Product(UUID.randomUUID(),
            "Product 1",
            "product 1 description",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 9, 5),//expiry date
            LocalDate.of(2024, 10, 10),//manufactured date
            LocalDate.of(2026, 9, 5)));//updated date

    //when and then
    mockMvc.perform(post(baseUrl + "/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1"))
            .andExpect(jsonPath("$.data.productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data.productQuantity").value(100))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-09-05"));
  }

  /**
   * Test for {@link ProductController#addProductWithInventory(CreateProductDto)} POST /api/v1/product/with-inventory
   * <p> Verifies that the addProductWithInventory method returns a product with inventory. </p>
   */
  @Test
  @DisplayName("Test for addProduct() with inventory POST /api/v1/product/with-inventory")
  void testAddProductSuccessWithInventory() throws Exception {
    //given
    var createRequest = new CreateProductDto(
            "Product 1",
            "product 1 description",
            "Category 1",
            100,
            10,
            new BigDecimal("100.0"),
            LocalDate.of(2024, 10, 10), //manufactured date
            LocalDate.of(2026, 9, 5)); //expiry date

    var json = objectMapper.writeValueAsString(createRequest);

    //mock the saveProduct method of the service class
    given(productService.saveProductWithInventory(any(Product.class))).willReturn(new Product(UUID.randomUUID(),
            "Product 1",
            "product 1 description",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 9, 5),//expiry date
            LocalDate.of(2024, 10, 10),//manufactured date
            LocalDate.of(2026, 9, 5)));//updated date

    //when and then
    mockMvc.perform(post(baseUrl + "/product/with-inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1"))
            .andExpect(jsonPath("$.data.productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data.productQuantity").value(100))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-09-05"));
  }

  @Test
  @DisplayName("Test for addProduct() with inventory using externalized service POST /api/v1/product/with-inventory/externalized")
  void testAddProductSuccessWithExternalizedService() throws Exception {
    //given
    var createRequest = new CreateProductDto(
            "Product 1",
            "product 1 description",
            "Category 1",
            100,
            10,
            new BigDecimal("100.0"),
            LocalDate.of(2024, 10, 10), //manufactured date
            LocalDate.of(2026, 9, 5)); //expiry date

    var json = objectMapper.writeValueAsString(createRequest);

    //mock the saveProduct method of the service class
    given(productService.saveProductWithInventoryUsingExternalizedService(any(Product.class))).willReturn(new Product(UUID.randomUUID(),
            "Product 1",
            "product 1 description",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 9, 5),//expiry date
            LocalDate.of(2024, 10, 10),//manufactured date
            LocalDate.of(2026, 9, 5)));//updated date

    //when and then
    mockMvc.perform(post(baseUrl + "/product/with-inventory/externalized")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Add One Success Using Externalized"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1"))
            .andExpect(jsonPath("$.data.productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data.productQuantity").value(100))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-09-05"));
  }

  /**
   * Test for {@link ProductController#updateProduct(UUID, UpdateProductDto)} PUT /api/v1/product/{productId} success
   * <p> Verifies that the updateProduct method returns a success. </p>
   */
  @Test
  @DisplayName("Test for updateProduct() PUT /api/v1/product/{productId} success")
  void testUpdateProductSuccess() throws Exception {
    //given
    UUID productId = productList.get(0).getProductId();

    var updateRequest = new UpdateProductDto(
            "Product 1 updated",
            "product 1 description updated",
            "product 1 category updated",
            new BigDecimal("100.0"),
            10,
            10);

    var json = objectMapper.writeValueAsString(updateRequest);

    //data to be returned by the service class
    var updatedProduct = new Product( productId,
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            10,
            "product 1 category updated",
            10,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));


    /**
     * it is important to use eq() when comparing the productId because the productId is a UUID
     * The eq() method is used in the given() statement in the controller test because UUIDs are
     * complex objects, and when using Mockito to match arguments, exact object equality
     * checks can cause issues if not handled properly.
     */
    given(productService.updateProduct(eq(productId), any(Product.class))).willReturn(updatedProduct);

    //when and then
    mockMvc.perform(put(baseUrl + "/product/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Update One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1 updated"))
            .andExpect(jsonPath("$.data.productCategory").value("product 1 category updated"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description updated"))
            .andExpect(jsonPath("$.data.productQuantity").value(10))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-10-10"));
  }

  /**
   * Test for {@link ProductController#deleteProduct(UUID)} DELETE /api/v1/product/{productId} success
   * <p> Verifies that the deleteProduct method returns a success. </p>
   */
  @Test
  @DisplayName("Test for deleteProduct() DELETE /api/v1/product/{productId} success")
  void deleteProductSuccess() throws Exception {
    //given
    UUID productId = productList.get(0).getProductId();
    doNothing().when(productService).deleteProduct(productId);
    //when and then
    mockMvc.perform(delete(baseUrl + "/product/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Delete One Success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  /**
   * Test for {@link ProductController#clearCache() } DELETE /api/v1/product/clear-cache success
   * <p> Verifies that the clearCache method returns a success. </p>
   */
  @Test
  @DisplayName("Test for clearCache() DELETE /api/v1/product/clear-cache success")
  void clearCache() throws Exception {
    //given
    doNothing().when(productService).clearAllCache();
    //when and then
    mockMvc.perform(delete(baseUrl + "/product/clear-cache")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Clear Cache Success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  /**
   * Test for {@link ProductController#addProductWithInventory(CreateProductDto)} POST /api/v1/product/with-inventory success
   * <p> Verifies that the addProductWithInventory method returns a product. </p>
   */
  @Test
  @DisplayName("Test for addProductWithInventory() POST /api/v1/product/with-inventory success")
  void testAddProductWithInventorySuccess() throws Exception {
    //given
    var createRequest = new CreateProductDto(
            "Product 1",
            "product 1 description",
            "Category 1",
            100,
            10,
            new BigDecimal("100.0"),
            LocalDate.of(2024, 10, 10), //manufactured date
            LocalDate.of(2026, 9, 5)); //expiry date

    var json = objectMapper.writeValueAsString(createRequest);

    //mock the saveProduct method of the service class
    given(productService.saveProductWithInventory(any(Product.class))).willReturn(new Product(
            UUID.randomUUID(),
            "Product 1",
            "product 1 description",
            new BigDecimal("100.0"),
            100,
            "Category 1",
            10,
            LocalDate.of(2026, 9, 5),//expiry date
            LocalDate.of(2024, 10, 10),//manufactured date
            LocalDate.of(2026, 9, 5)));//updated date

    //when and then
    mockMvc.perform(post(baseUrl + "/product/with-inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1"))
            .andExpect(jsonPath("$.data.productCategory").value("Category 1"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description"))
            .andExpect(jsonPath("$.data.productQuantity").value(100))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-09-05"));
  }

  /**
   * Test for {@link ProductController#updateProductWithInventory(UUID, UpdateProductDto)} PUT /api/v1/product/{productId}/with-inventory success
   * <p> Verifies that the updateProductWithInventory method returns a success. </p>
   */
  @Test
  @DisplayName("Test for updateProductWithInventory() PUT /api/v1/product/{productId}/with-inventory success")
  void testUpdateProductWithInventorySuccess() throws Exception {
    //given
    UUID productId = productList.get(0).getProductId();

    var updateRequest = new UpdateProductDto(
            "Product 1 updated",
            "product 1 description updated",
            "product 1 category updated",
            new BigDecimal("100.0"),
            10,
            10);

    var json = objectMapper.writeValueAsString(updateRequest);

    //data to be returned by the service class
    var updatedProduct = new Product( productId,
            "Product 1 updated",
            "product 1 description updated",
            new BigDecimal("100.0"),
            10,
            "product 1 category updated",
            10,
            LocalDate.of(2026, 10, 10),
            LocalDate.of(2021, 9, 5),
            LocalDate.of(2021, 9, 5));

    /**
     * it is important to use eq() when comparing the productId because the productId is a UUID
     * The eq() method is used in the given() statement in the controller test because UUIDs are
     * complex objects, and when using Mockito to match arguments, exact object equality
     * checks can cause issues if not handled properly.
     */
    given(productService.updateProductWithInventory(eq(productId), any(Product.class))).willReturn(updatedProduct);

    //when and then
    mockMvc.perform(put(baseUrl + "/product/" + productId + "/with-inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Update One Success"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value("Product 1 updated"))
            .andExpect(jsonPath("$.data.productCategory").value("product 1 category updated"))
            .andExpect(jsonPath("$.data.productDescription").value("product 1 description updated"))
            .andExpect(jsonPath("$.data.productQuantity").value(10))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.expiryDate").value("2026-10-10"));
  }

  /**
   * Test for {@link ProductController#bulkDeleteProducts(List)} DELETE /api/v1/product/bulk-delete success
   * <p> Verifies that the bulkDeleteProducts method returns a success. </p>
   */
  @Test
  @DisplayName("Test for bulkDeleteProducts() DELETE /api/v1/product/bulk-delete success")
  void testBulkDeleteProductsSuccess() throws Exception {
    //given
    List<UUID> productIds = Arrays.asList(productList.get(0).getProductId(), productList.get(1).getProductId());
    doNothing().when(productService).bulkDeleteProducts(productIds);

    //when and then
    mockMvc.perform(delete(baseUrl + "/product/bulk-delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(productIds))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Bulk Delete Success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  /**
   * Test for {@link ProductController#bulkDeleteProducts(List)} DELETE /api/v1/product/bulk-delete when some ids are not found
   * <p> Verifies that the bulkDeleteProducts method returns a ProductNotFoundException. </p>
   */
  @Test
  @DisplayName("Test for bulkDeleteProducts() DELETE /api/v1/product/bulk-delete when some ids are not found")
  void testBulkDeleteProductsWhenSomeIdsNotFound() throws Exception {
    //given
    var missingProductId = UUID.randomUUID();
    List<UUID> productIds = Arrays.asList(productList.get(0).getProductId(), missingProductId);

    //simulate service throwing exception
    doThrow(new ProductNotFoundException(List.of(missingProductId))).when(productService).bulkDeleteProducts(productIds);

    //when and then
    mockMvc.perform(delete(baseUrl + "/product/bulk-delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(productIds))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Product not found with ids: [" + missingProductId + "]"))
            .andExpect(jsonPath("$.flag").value(false));
  }

  /**
   * Test for {@link ProductController#deleteProductWithInventory(UUID)} DELETE /api/v1/product/delete/with-inventory/{productId} success
   * <p> Verifies that the deleteProductWithInventory method returns a success. </p>
   */
  @Test
  @DisplayName("Test for deleteProductWithInventory() DELETE /api/v1/product/delete/with-inventory/{productId} success")
  void testDeleteProductWithInventorySuccess() throws Exception {
    //given
    UUID productId = productList.get(0).getProductId();
    doNothing().when(productService).deleteProductWithInventory(productId);
    //when and then
    mockMvc.perform(delete(baseUrl + "/product/delete/with-inventory/" + productId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Delete One Success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  /**
   * Test for {@link ProductController#bulkDeleteProductsWithInventories(List)} DELETE /api/v1/product/bulk-delete/with-inventory
   * <p> Verifies that the bulkDeleteProductWithInventories method returns a successful </p>
   */
  @Test
  @DisplayName("Test for bulkDeleteProductsWithInventories() DELETE /api/v1/product/bulk-delete/with-inventory success")
  void testBulkDeleteProductsWithInventoriesSuccess() throws Exception {
    //given
    List<UUID> productIds = Arrays.asList(productList.get(0).getProductId(), productList.get(1).getProductId());
    doNothing().when(productService).bulkDeleteProductsWithInventories(productIds);

    //when and then
    mockMvc.perform(delete(baseUrl + "/product/bulk-delete/with-inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(productIds))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Bulk Delete With Inventories Success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

}