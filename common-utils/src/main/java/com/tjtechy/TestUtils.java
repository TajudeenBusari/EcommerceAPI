/**
 * Not in use currently.
 */
package com.tjtechy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Component
@RequiredArgsConstructor
public class TestUtils {
  private final ObjectMapper objectMapper;

  /**
   * Create a product dynamically using mockMvc.
   */
  public Map<String, Object> createProduct(MockMvc mockMvc, String apiUrl) throws Exception{
    String uniqueProductName = "Product" + System.currentTimeMillis();

    /**
     * Since we are using Map.of() to create CreateProductDto, this class
     * does not need the CreateProductDto class to be imported to be
     * used in the common-utils module or other modules.
     * What is important is that the keys in the map are the same as the
     * corresponding fields in the CreateProductDto class.
     */
    var createProductRequest =  Map.of(
            "productName", uniqueProductName,
            "productDescription", "This is a test product",
            "productCategory", "Electronics",
            "productQuantity", 100,
            "availableStock", 50,
            "productPrice", BigDecimal.valueOf(1000),
            "manufacturedDate", LocalDate.now(),
            "expiryDate", LocalDate.now().plusDays(30)
    );

    var json = objectMapper.writeValueAsString(createProductRequest);

    var result = mockMvc.perform(post(apiUrl + "/product")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Add One Success"))
            .andReturn();

    //Extract product details from response
    var response = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    var productDetails = (Map<String, Object>) response.get("data");

    //validate that the product ID is present in the response
    var productId = (String) productDetails.get("productId");
    if (productId == null || productId.isEmpty()) {
      throw new RuntimeException("Product ID is missing in the response");
    }
    System.out.println("Product ID: " + productId);
    return productDetails;

  }
  /**
   * Create a product dynamically using WebTestClient.
   * This is used for reactive applications.
   */

  public Map<String, Object> createProductByWebTestClient(WebTestClient webTestClient, String apiUrl) throws Exception{
    String uniqueProductName = "Product" + System.currentTimeMillis();

    var createProductRequest =  Map.of(
            "productName", uniqueProductName,
            "productDescription", "This is a test product",
            "productCategory", "Electronics",
            "productQuantity", 100,
            "availableStock", 50,
            "productPrice", BigDecimal.valueOf(1000),
            "manufacturedDate", LocalDate.now(),
            "expiryDate", LocalDate.now().plusDays(30)
    );

    var json = objectMapper.writeValueAsString(createProductRequest);

    var result = webTestClient.post()
            .uri(apiUrl + "/product")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Map.class)
            .returnResult()
            .getResponseBody();

    //Extract product details from response
    var productDetails = (Map<String, Object>) result.get("data");

    //validate that the product ID is present in the response
    var productId = (String) productDetails.get("productId");
    if (productId == null || productId.isEmpty()) {
      throw new RuntimeException("Product ID is missing in the response");
    }
    System.out.println("Product ID: " + productId);
    return productDetails;
  }
}
