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
import com.tjtechy.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.tjtechy.system.Result;

import java.util.UUID;


@RestController
@RequestMapping("${api.endpoint.base-url}/product")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public Result getAllProducts(){
    var products = productService.getAllProducts();
    //map to dto
    var productDtos = ProductMapper.mapFromProductListToProductDtoList(products);
    return new Result("Get All Success", true, productDtos, StatusCode.SUCCESS);
  }

  @GetMapping("/{productId}")
  public Result getProductById(@PathVariable UUID productId){
    var product = productService.getProductById(productId);
    //map to dto
    var productDto = ProductMapper.mapFromProductToProductDto(product);
    return new Result("Get One Success", true, productDto, StatusCode.SUCCESS);
  }

  @PostMapping
  public Result addProduct(@Valid @RequestBody CreateProductDto createProductDto){
    var product = ProductMapper.mapFromCreateProductDtoToProduct(createProductDto);
    var savedProduct = productService.saveProduct(product);
    //map to dto
    var savedProductDto = ProductMapper.mapFromProductToProductDto(savedProduct);
    return new Result("Add One Success", true, savedProductDto, StatusCode.SUCCESS);
  }

  @PutMapping("/{productId}")
  public Result updateProduct(@PathVariable UUID productId, @Valid @RequestBody UpdateProductDto updateProductDto){
    var product = ProductMapper.mapFromUpdateProductDtoToProduct(updateProductDto);
    var updatedProduct = productService.updateProduct(productId, product);
    //map to dto
    var updatedProductDto = ProductMapper.mapFromProductToProductDto(updatedProduct);
    return new Result("Update One Success", true, updatedProductDto, StatusCode.SUCCESS);
  }

  @DeleteMapping("/{productId}")
  public Result deleteProduct(@PathVariable UUID productId){
    productService.deleteProduct(productId);
    return new Result("Delete One Success", true, null, StatusCode.SUCCESS);
  }

  @DeleteMapping("/clear-cache")
  public Result clearCache(){
    productService.clearAllCache();
    return new Result("Clear Cache Success", true, null, StatusCode.SUCCESS);
  }


}
