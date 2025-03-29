/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import redis.embedded.RedisServer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cloud.config.enabled=false",//disable spring cloud config
        "eureka.client.enabled=false",//disable eureka client
})
public class ProductControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  private  String API_URL;

  private static RedisServer redisServer;

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
          .withDatabaseName("productdb")
          .withUsername("testuser")
          .withPassword("testpassword");

  @BeforeAll
  static void startContainers(){
    postgreSQLContainer.start();

    //wait for PostgreSQL to be ready
    while (!postgreSQLContainer.isRunning()){
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());

    //start redis server on a specific port
    //int redisPort = 6379;
    redisServer = new RedisServer(); //bind to a random port
    redisServer.start();

    //wait for redis server to be ready
    while (!redisServer.isActive()){
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    //System.setProperty("spring.redis.host", String.valueOf(redisPort));

    System.setProperty("spring.redis.port", String.valueOf(redisServer.ports().get(0)));

  }

  @AfterAll
  static void stopContainers(){
    postgreSQLContainer.stop();
    redisServer.stop();
  }


  /**
   * Helper method to create a product
   * @return
   * @throws Exception
   */
  private Map<String, Object> createProduct() throws Exception {
    String uniqueProductName = "Product " + System.currentTimeMillis(); //this ensures the product name is unique
    var createProductDto = new CreateProductDto(
            uniqueProductName,
            "Product 1 description",
            "Product 1 category",
            10,
            10,
            BigDecimal.valueOf(1000.00),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
    );
    var json = objectMapper.writeValueAsString(createProductDto);
    var result = mockMvc.perform(post(API_URL + "/product")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andExpect(jsonPath("$.data.productId").isNotEmpty())
            .andExpect(jsonPath("$.data.productName").value(uniqueProductName))
            .andExpect(jsonPath("$.data.productDescription").value("Product 1 description"))
            .andExpect(jsonPath("$.data.productCategory").value("Product 1 category"))
            .andExpect(jsonPath("$.data.productQuantity").value(10))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.productPrice").value(1000.00))
            //this is not returned in the Dto.
            //.andExpect(jsonPath("$.data.manufacturedDate").isNotEmpty())
            .andExpect(jsonPath("$.data.expiryDate").isNotEmpty())
            .andReturn();

    //Extract the product details from response
    var responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    return (Map<String, Object>) responseBody.get("data");
  }



  @Test
  @DisplayName("Check Add Product (POST /api/v1/product)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testCreateProductSuccess() throws Exception {

   var productDetails = createProduct();

   //Extract the dynamically generated product name
    var expectedProductName = (String) productDetails.get("productName");

   //Assert the returned product details
    assert productDetails != null;
    assert expectedProductName != null && expectedProductName.startsWith("Product ");
    assert productDetails.get("productDescription").equals("Product 1 description");
    assert productDetails.get("productCategory").equals("Product 1 category");
    assert productDetails.get("productQuantity").equals(10);
    assert productDetails.get("availableStock").equals(10);
    assert productDetails.get("productPrice").equals(1000.00);
    assert productDetails.get("expiryDate") != null;

  }

  @Test
  @DisplayName("Check Get Product by Id (GET /api/v1/product/{productId})")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testGetProductByIdSuccess() throws Exception {

    var productDetails = createProduct();
    var productId = productDetails.get("productId");

    //Extract the dynamically generated product name
    var expectedProductName = (String) productDetails.get("productName");

    //Get the product by id
    var result = mockMvc.perform(get(API_URL + "/product/" + productId))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Get One Success"))
            .andExpect(jsonPath("$.data.productId").value(productId))
            .andExpect(jsonPath("$.data.productName").value(expectedProductName))
            .andExpect(jsonPath("$.data.productDescription").value("Product 1 description"))
            .andExpect(jsonPath("$.data.productCategory").value("Product 1 category"))
            .andExpect(jsonPath("$.data.productQuantity").value(10))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.productPrice").value(1000.00))
            .andExpect(jsonPath("$.data.expiryDate").isNotEmpty())
            .andReturn();
  }

  @Test
  @DisplayName("Check Get all Products (GET /api/v1/product)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testGetAllProductsSuccess() throws Exception {
    //Create 3 products
    createProduct();
    createProduct();
    createProduct();

    //Get all products
    var result = mockMvc.perform(get(API_URL + "/product"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Get All Success"))
            .andExpect(jsonPath("$.data").isArray())
            /**
            if you specify a length, you get error because the length is dynamically changing
             as result of the products created in the test that is called before each test.
             So new products are created before each test and the length of the products array
              will be different each time.
            * */
            .andExpect(jsonPath("$.data.length()").isNotEmpty())
            .andReturn();
    var responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);

    //print the id of each product
    var products = (List<Map<String, Object>>) responseBody.get("data");
    for (var product : products){
      System.out.println("Product ID: " + product.get("productId"));
//      System.out.println("Product Name: " + product.get("productName"));
    }
  }

  @Test
  @DisplayName("Check Update Product by Id (PUT /api/v1/product/{productId})")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testUpdateProductByIdSuccess() throws Exception {
    var productDetails = createProduct();
    var productId = productDetails.get("productId");


    //Update the product
    var updateProductDto = new UpdateProductDto(
            "Updated Product Name",
            "Updated Product Description",
            "Updated Product Category",
            BigDecimal.valueOf(1000.00),
            20,
            10
    );
    var json = objectMapper.writeValueAsString(updateProductDto);
    var result = mockMvc.perform(put(API_URL + "/product/" + productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Update One Success"))
            .andExpect(jsonPath("$.data.productId").value(productId))
            .andExpect(jsonPath("$.data.productName").value("Updated Product Name"))
            .andExpect(jsonPath("$.data.productDescription").value("Updated Product Description"))
            .andExpect(jsonPath("$.data.productCategory").value("Updated Product Category"))
            .andExpect(jsonPath("$.data.productQuantity").value(20))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.productPrice").value(1000.00))
            .andExpect(jsonPath("$.data.expiryDate").isNotEmpty())
            .andReturn();
  }

  @Test
  @DisplayName("Check Delete Product by Id (DELETE /api/v1/product/{productId})")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteProductByIdSuccess() throws Exception {
    var productDetails = createProduct();
    var productId = productDetails.get("productId");

    //Delete the product
    mockMvc.perform(delete(API_URL + "/product/" + productId))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Delete One Success"));
  }


}
