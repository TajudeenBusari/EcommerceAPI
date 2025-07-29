/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.controller;

import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;
import com.tjtechy.product_service.mapper.ProductMapper;
import com.tjtechy.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.tjtechy.Result;
import com.tjtechy.StatusCode;


@RestController
@RequestMapping("${api.endpoint.base-url}/product")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {

    this.productService = productService;
  }

  /**
   * Endpoint to get all products.
   *
   * @return a Result object containing a list of all products
   */
  @Operation(summary = "Get All Products", description = "Retrieve all products from the product service",
  responses = {
          @ApiResponse(responseCode = "200", description = "Get All Success"),
  })
  @GetMapping
  public Result getAllProducts(){
    var products = productService.getAllProducts();
    //map to dto
    var productDtos = ProductMapper.mapFromProductListToProductDtoList(products);
    return new Result("Get All Success", true, productDtos, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to get a product by its ID.
   *
   * @param productId the UUID of the product to retrieve
   * @return a Result object containing the product details
   */
  @Operation(summary = "Get Product By ID", description = "Retrieve a product by its ID from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Get One Success"),
          }
  )
  @GetMapping("/{productId}")
  public Result getProductById(@PathVariable UUID productId){
    var product = productService.getProductById(productId);
    //map to dto
    var productDto = ProductMapper.mapFromProductToProductDto(product);
    return new Result("Get One Success", true, productDto, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to add a new product.
   *
   * @param createProductDto the DTO containing the product details to create
   * @return a Result object containing the created product details
   */
  @Operation(summary = "Add Product", description = "Add a new product to the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Add One Success"),
          })
  @PostMapping
  public Result addProduct(@Valid @RequestBody CreateProductDto createProductDto){
    var product = ProductMapper.mapFromCreateProductDtoToProduct(createProductDto);
    var savedProduct = productService.saveProduct(product);
    //map to dto
    var savedProductDto = ProductMapper.mapFromProductToProductDto(savedProduct);
    return new Result("Add One Success", true, savedProductDto, StatusCode.SUCCESS);
  }
  /**
   * Endpoint to add a new product with inventory.
   *
   * @param createProductDto the DTO containing the product details to create
   * @return a Result object containing the created product details
   */
@Operation(summary = "Add Product With Inventory", description = "Add a new product with inventory to the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Add One Success"),
          })
  @PostMapping("/with-inventory")
  public Result addProductWithInventory(@Valid @RequestBody CreateProductDto createProductDto){
    var product = ProductMapper.mapFromCreateProductDtoToProduct(createProductDto);
    var savedProduct = productService.saveProductWithInventory(product);
    //map to dto
    var savedProductDto = ProductMapper.mapFromProductToProductDto(savedProduct);
    return new Result("Add One Success", true, savedProductDto, StatusCode.SUCCESS);
  }

  @Operation(summary = "Add Product With Inventory Externalized", description = "Add a new product with inventory using an externalized inventory service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Add One Success Using Externalized"),
          })

  @PostMapping("/with-inventory/externalized")
  public Result addProductWithInventoryUsingExternalizedService(@Valid @RequestBody CreateProductDto createProductDto){
    var product = ProductMapper.mapFromCreateProductDtoToProduct(createProductDto);
    var savedProduct = productService.saveProductWithInventoryUsingExternalizedService(product);
    //map to dto
    var savedProductDto = ProductMapper.mapFromProductToProductDto(savedProduct);
    return new Result("Add One Success Using Externalized", true, savedProductDto, StatusCode.SUCCESS);
  }

  @Operation(summary = "Update Product With Inventory", description = "Update an existing product with inventory",
          responses = {
          @ApiResponse(responseCode = "200", description = "Update One Success"),
          })
  @PutMapping("/{productId}/with-inventory")
  public Result updateProductWithInventory(@PathVariable UUID productId, @Valid @RequestBody UpdateProductDto updateProductDto){
    var product = ProductMapper.mapFromUpdateProductDtoToProduct(updateProductDto);
    var updatedProduct = productService.updateProductWithInventory(productId, product);
    //map to dto
    var updatedProductDto = ProductMapper.mapFromProductToProductDto(updatedProduct);
    return new Result("Update One Success", true, updatedProductDto, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to update an existing product with inventory using an externalized service.
   *
   * @param productId the UUID of the product to update
   * @param updateProductDto the DTO containing the updated product details
   * @return a Result object containing the updated product details
   */
  @Operation(summary = "Update Product With Externalized Inventory ",
          description = "Update an existing product with inventory using an externalized inventory service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Update One Success Using Externalized"),
          })
  @PutMapping("/{productId}/with-inventory/externalized")
  public Result updateProductWithInventoryUsingExternalizedService(@PathVariable UUID productId, @Valid @RequestBody UpdateProductDto updateProductDto){
    var product = ProductMapper.mapFromUpdateProductDtoToProduct(updateProductDto);
    var updatedProduct = productService.updateProductWithInventoryUsingExternalizedService(productId, product);
    //map to dto
    var updatedProductDto = ProductMapper.mapFromProductToProductDto(updatedProduct);
    return new Result("Update One Success Using Externalized", true, updatedProductDto, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to update an existing product.
   *
   * @param productId the UUID of the product to update
   * @param updateProductDto the DTO containing the updated product details
   * @return a Result object containing the updated product details
   */
@Operation(summary = "Update Product", description = "Update an existing product in the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Update One Success"),
          })
  @PutMapping("/{productId}")
  public Result updateProduct(@PathVariable UUID productId, @Valid @RequestBody UpdateProductDto updateProductDto){
    var product = ProductMapper.mapFromUpdateProductDtoToProduct(updateProductDto);
    var updatedProduct = productService.updateProduct(productId, product);
    //map to dto
    var updatedProductDto = ProductMapper.mapFromProductToProductDto(updatedProduct);
    return new Result("Update One Success", true, updatedProductDto, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to delete a product by its ID.
   *
   * @param productId the UUID of the product to delete
   * @return a Result object indicating the success of the deletion
   */
@Operation(summary = "Delete Product", description = "Delete a product by its ID from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Delete One Success"),
          })
  @DeleteMapping("/{productId}")
  public Result deleteProduct(@PathVariable UUID productId){
    productService.deleteProduct(productId);
    return new Result("Delete One Success", true, null, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to clear all cached product data.
   *
   * @return a Result object indicating the success of the cache clearing
   */
@Operation(summary = "Clear Cache", description = "Clear all cached product data",
          responses = {
          @ApiResponse(responseCode = "200", description = "Clear Cache Success"),
          })
  @DeleteMapping("/clear-cache")
  public Result clearCache(){
    productService.clearAllCache();
    return new Result("Clear Cache Success", true, null, StatusCode.SUCCESS);
  }
  /**
   * Endpoint to bulk delete products by their IDs.
   *
   * @param productIds the list of UUIDs of the products to delete
   * @return a Result object indicating the success of the bulk deletion
   */
@Operation(summary = "Bulk Delete Products", description = "Bulk delete products by their IDs from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Bulk Delete Success"),
          })
  @DeleteMapping("/bulk-delete")
  public Result bulkDeleteProducts(@RequestBody List<UUID> productIds){
    productService.bulkDeleteProducts(productIds);
    return new Result("Bulk Delete Success", true, null, StatusCode.SUCCESS);
  }


  @DeleteMapping("/delete/with-inventory/{productId}")
  @Operation(summary = "Delete Product With Inventory", description = "Delete a product with its inventory by its ID from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Delete One Success"),
          })
  public Result deleteProductWithInventory(@PathVariable UUID productId){
    productService.deleteProductWithInventory(productId);
    return new Result("Delete One Success", true, null, StatusCode.SUCCESS);
  }

  //TODO: Change to return Mono<Result> for reactive programming
  @Operation(summary = "Delete Product With Inventory Using Externalized Service",
          description = "Delete a product with its inventory using an externalized inventory service by its ID from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Delete One Success Using Externalized"),
          })
  @DeleteMapping("/delete/with-inventory/externalized/{productId}")
  public Result deleteProductWithInventoryUsingExternalizedService(@PathVariable UUID productId){
    productService.deleteProductWithInventoryUsingExternalizedService(productId);
    return new Result("Delete One Success Using Externalized", true, null, StatusCode.SUCCESS);
  }

  @Operation(summary = "Bulk Delete Products With Inventories",
          description = "Bulk delete products with their inventories by their IDs from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Bulk Delete With Inventories Success"),
          })
  @DeleteMapping("/bulk-delete/with-inventory")
  public Result bulkDeleteProductsWithInventories(@RequestBody List<UUID> productIds){
    productService.bulkDeleteProductsWithInventories(productIds);
    return new Result("Bulk Delete With Inventories Success", true, null, StatusCode.SUCCESS);
  }

  @Operation(summary = "Bulk Delete Products With Inventories Using Externalized Service",
          description = "Bulk delete products with their inventories using an externalized inventory service by their IDs from the product service",
          responses = {
          @ApiResponse(responseCode = "200", description = "Bulk Delete With Inventories Using Externalized Success"),
          })
  @DeleteMapping("/bulk-delete/with-inventory/externalized")
  public Result bulkDeleteProductsWithInventoryExternalized(@RequestBody List<UUID> productIds){
    productService.bulkDeleteProductsWithInventoriesUsingExternalizedService(productIds);
    return new Result("Bulk Delete With Inventories Using Externalized Success", true, null, StatusCode.SUCCESS);
  }


}
