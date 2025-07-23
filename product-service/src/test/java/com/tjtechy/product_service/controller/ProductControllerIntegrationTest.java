/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tjtechy.product_service.config.InventoryServiceConfig;
import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;


import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@Tag("ProductControllerIntegrationTest")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "api.endpoint.base-url=/api/v1",
        "spring.cloud.config.enabled=false",//disable spring cloud config
        "eureka.client.enabled=false",//disable eureka client
        "spring.datasource.url=jdbc:tc:postgresql:15.0:///productdb",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.datasource.username=testuser",
        "spring.datasource.password=testpassword",
        "redis.enabled=false", //disable redis
        "spring.cache.type=none", //disable caching
        "spring.jpa.hibernate.ddl-auto=create-drop", // Use create-drop for testing
        "spring.jpa.show-sql=true", // Show SQL queries in logs
        "spring.jpa.properties.hibernate.format_sql=true" // Format SQL queries
})
@WireMockTest(httpPort = 9562)
@Import(ProductControllerIntegrationTest.TestInventoryClientConfig.class)

/**
 * NOTE: This test class is deprecated but will not be removed for reference purposes.
 * For new integration tests, use the new test class ProductServiceControllerIntegrationTest.
 */
public class ProductControllerIntegrationTest {
  /**
   * MockMvc is used to test Spring boot mvc controllers
   * without starting a full HTTP server.
   * Simulates HTTP requests and responses.
   */
  @Autowired
  private MockMvc mockMvc;


  @Autowired
  private ObjectMapper objectMapper;

  @Value("${api.endpoint.base-url}")
  private  String API_URL;


  @Autowired
  private WebTestClient webClient;


  @Autowired
  private InventoryServiceConfig inventoryServiceConfig;

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.0")
          .withDatabaseName("productdb")
          .withUsername("testuser")
          .withPassword("testpassword")
          .waitingFor(Wait.forListeningPort());

  @DynamicPropertySource
  static void dynamicProperties(DynamicPropertyRegistry registry) {
    //dynamically inject the database url
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
    //override the base url used in WebClient
    registry.add("inventory-service.base-url", () -> "http://localhost:9562/api/v1");
  }

  @TestConfiguration
  static class TestInventoryClientConfig{
    @Bean
    public WebClient inventoryWebClient(WebClient.Builder builder) {
      return builder.baseUrl("http://localhost:9562/api/v1").build();
    }
  }




  @BeforeAll
  static void startContainers() {
    postgreSQLContainer.start();

    //wait for PostgreSQL to be ready
    while (!postgreSQLContainer.isRunning()){
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @AfterAll
  static void stopContainers(){
    postgreSQLContainer.stop();

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
            .andExpect(jsonPath("$.data.availableStock").value(20)) //available stock is now same as product quantity
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

  @Test
  @DisplayName("Check AddProductWithInventory (POST /api/v1/product/with-inventory)")
  void testAddProductWithInventory() throws Exception {
   var createProductDto = new CreateProductDto(
            "Product 1",
            "Product 1 description",
            "Product 1 category",
            10,
            10,
            BigDecimal.valueOf(1000.00),
            LocalDate.now(),
            LocalDate.now().plusYears(1)
    );
    var json = objectMapper.writeValueAsString(createProductDto);

    var result = mockMvc.perform(post(API_URL + "/product/with-inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andExpect(jsonPath("$.data.availableStock").value(10))
            .andExpect(jsonPath("$.data.productQuantity").value(10)).andReturn();
    var responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    var productDetails = (Map<String, Object>) responseBody.get("data");
    var productId = productDetails.get("productId");
    System.out.println("Product ID: " + productId);

  }

  @Test
  @DisplayName("Check UpdateProductWithInventory (PUT /api/v1/product/{productId}/with-inventory)")
  void testUpdateProductWithInventory() throws Exception {
    var productDetails = createProduct();
    var productId = productDetails.get("productId");

    //Update the product
    var updateProductDto = new UpdateProductDto(
            "Updated1 Product Name",
            "Updated1 Product Description",
            "Updated1 Product Category",
            BigDecimal.valueOf(1000.00),
            20,
            10
    );
    var json = objectMapper.writeValueAsString(updateProductDto);
    var result = mockMvc.perform(put(API_URL + "/product/" + productId + "/with-inventory")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Update One Success"))
            .andExpect(jsonPath("$.data.productId").value(productId))
            .andExpect(jsonPath("$.data.productName").value("Updated1 Product Name"))
            .andExpect(jsonPath("$.data.productDescription").value("Updated1 Product Description"))
            .andExpect(jsonPath("$.data.productCategory").value("Updated1 Product Category"))
            .andExpect(jsonPath("$.data.productQuantity").value(20))
            .andExpect(jsonPath("$.data.availableStock").value(20)) //available stock is now same as product quantity
            .andExpect(jsonPath("$.data.productPrice").value(1000.00))
            .andExpect(jsonPath("$.data.expiryDate").isNotEmpty())
            .andReturn();
    var responseBody = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    var updatedProductDetails = (Map<String, Object>) responseBody.get("data");
    var updatedProductId = updatedProductDetails.get("productId");
    System.out.println("Updated Product ID: " + updatedProductId);
  }

  @Test
  @DisplayName("Check bulkDeleteProducts (DELETE /api/v1/product/bulk-delete)")
  void testBulkDeleteProducts() throws Exception {
    // Create 3 products
    var product1 = createProduct();
    var product2 = createProduct();
    var product3 = createProduct();

    // Get the product IDs
    var productId1 = (String) product1.get("productId");
    var productId2 = (String) product2.get("productId");
    var productId3 = (String) product3.get("productId");

    // Perform bulk delete
    mockMvc.perform(delete(API_URL + "/product/bulk-delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(productId1, productId2, productId3))))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Bulk Delete Success"));
  }

  @Test
  void testDeleteProductWithInventory() throws Exception {
    var productDetails = createProduct();
    var productId = productDetails.get("productId");

    mockMvc.perform(delete(API_URL + "/product/delete/with-inventory/" + productId)
    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Delete One Success"));
  }

  @Test
  @DisplayName("Check AddProductWithInventoryExternalized (POST /api/v1/product/with-inventory/externalized)")
  void testAddProductWithInventoryExternalizedSuccess() throws Exception {
    var createProductDto = new CreateProductDto(
            "Product 1 Externalized",
            "Product 1 description Externalized",
            "Product 1 category Externalized",
            10,
            10,
            BigDecimal.valueOf(1000.00),
            LocalDate.now(),
            LocalDate.now().plusYears(1));
    var json = objectMapper.writeValueAsString(createProductDto);//convert to JSON string
  }


  /**This is a nested class to test the delete product with inventory endpoint
   * It has it own configuration for the inventory service and uses the WireMock server
   * This test is to check the delete product with inventory endpoint
   * It will first create a product, then create an inventory for that product,
   * and then delete the product with the inventory.
   */
  @Nested
  class DeleteProductWithInventoryTest {

    private static final String INVENTORY_BASE_API_URL = "/inventory/";

    //TODO: Fix the GET request to the inventory service
    /**IMPORTANT NOTE:
     * This test still has some issues with the GET request to the inventory service and
     * the DELETE request to the inventory service. But it still works.
     *
     */

    @Test
    void testDeleteProductWithInventory() throws Exception {
      var productDetails = createProduct();
      var productId = productDetails.get("productId");
      var inventoryId = 1L; //mock inventory id
      var reservedQuantity = 5;

      //mock the GET request to the inventory service
      WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(INVENTORY_BASE_API_URL + "internal/product/" + productId))
              .willReturn(WireMock.okJson(String.format("""
                      {
                      flag": true,
                      "message": "Inventory with productId retrieved successfully",
                      "data": {"inventoryId": %d, "productId": "%s", "reservedQuantity": %d},
                      "code": 200
                      }
                      """, inventoryId, productId, reservedQuantity))));

      //mock the DELETE request to the inventory service
      WireMock.stubFor(WireMock.delete(WireMock.urlEqualTo(INVENTORY_BASE_API_URL  + inventoryId))
              .willReturn(WireMock.okJson("""
                      {
                      "flag": true,
                      "message": "Inventory deleted successfully",
                      "data": null,
                      "code": 200
                      }
                      """)));
      // Perform the delete product with inventory request
      mockMvc.perform(delete(API_URL + "/product/delete/with-inventory/" + productId)
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(jsonPath("$.flag").value(true))
              .andExpect(jsonPath("$.message").value("Delete One Success"));

        }
    }
  }




