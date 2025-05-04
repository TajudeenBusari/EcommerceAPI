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
  @PostMapping("/with-inventory")
  public Result addProductWithInventory(@Valid @RequestBody CreateProductDto createProductDto){
    var product = ProductMapper.mapFromCreateProductDtoToProduct(createProductDto);
    var savedProduct = productService.saveProductWithInventory(product);
    //map to dto
    var savedProductDto = ProductMapper.mapFromProductToProductDto(savedProduct);
    return new Result("Add One Success", true, savedProductDto, StatusCode.SUCCESS);
  }

  @PutMapping("/{productId}/with-inventory")
  public Result updateProductWithInventory(@PathVariable UUID productId, @Valid @RequestBody UpdateProductDto updateProductDto){
    var product = ProductMapper.mapFromUpdateProductDtoToProduct(updateProductDto);
    var updatedProduct = productService.updateProductWithInventory(productId, product);
    //map to dto
    var updatedProductDto = ProductMapper.mapFromProductToProductDto(updatedProduct);
    return new Result("Update One Success", true, updatedProductDto, StatusCode.SUCCESS);
  }

  /**
   * Endpoint to update an existing product.
   *
   * @param productId the UUID of the product to update
   * @param updateProductDto the DTO containing the updated product details
   * @return a Result object containing the updated product details
   */
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
  @DeleteMapping("/bulk-delete")
  public Result bulkDeleteProducts(@RequestBody List<UUID> productIds){
    productService.bulkDeleteProducts(productIds);
    return new Result("Bulk Delete Success", true, null, StatusCode.SUCCESS);
  }

  @DeleteMapping("/delete/with-inventory/{productId}")
  public Result deleteProductWithInventory(@PathVariable UUID productId){
    productService.deleteProductWithInventory(productId);
    return new Result("Delete One Success", true, null, StatusCode.SUCCESS);
  }
  @DeleteMapping("/bulk-delete/with-inventory")
  public Result bulkDeleteProductsWithInventories(@RequestBody List<UUID> productIds){
    productService.bulkDeleteProductsWithInventories(productIds);
    return new Result("Bulk Delete With Inventories Success", true, null, StatusCode.SUCCESS);
  }


}
