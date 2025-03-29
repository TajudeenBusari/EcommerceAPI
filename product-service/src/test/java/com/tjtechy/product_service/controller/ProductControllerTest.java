/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.RedisCacheConfig;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;
import com.tjtechy.product_service.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.config.client.ConfigServerBootstrapper;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
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
@ImportAutoConfiguration(exclude = {
        RedisCacheConfig.class,
        EurekaClientAutoConfiguration.class,
        ConfigServerBootstrapper.class
})
class ProductControllerTest {

  @MockitoBean
  private ProductService productService;

  @MockitoBean
  private RedisConnectionFactory redisConnectionFactory;

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



  @Test
  void getAllProductsSuccess() throws Exception {
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

  @Test
  void getProductByIdSuccess() throws Exception {
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

  @Test
  void addProductSuccess() throws Exception {
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

  @Test
  void updateProductSuccess() throws Exception {
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

  @Test
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

  @Test
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
}