/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.mapper;
import com.tjtechy.product_service.entity.Product;
import com.tjtechy.product_service.entity.dto.CreateProductDto;
import com.tjtechy.product_service.entity.dto.ProductDto;
import com.tjtechy.product_service.entity.dto.UpdateProductDto;

import java.util.List;


public class ProductMapper {

  /**
   * Converts a {@link Product} entity to a {@link ProductDto}.
   * <p>
   * This method maps the essential fields from the {@link Product} entity to its corresponding DTO.
   * The DTO is used to transfer data in a structured manner without exposing the entity directly.
   * </p>
   *The order of parameters in the productDto constructor is the way data will be returned in the response
   * @param product The {@link Product} entity to be mapped. Must not be {@code null}.
   * @return A {@link ProductDto} containing the mapped fields from the provided {@link Product} entity.
   * @throws NullPointerException if the provided product is {@code null}.
   */
  public static ProductDto mapFromProductToProductDto(Product product) {
    return new ProductDto(
            product.getProductId(),
            product.getProductName(),
            product.getProductCategory(),
            product.getProductDescription(),
            product.getProductQuantity(),
            product.getAvailableStock(),
            product.getExpiryDate(),
            product.getProductPrice()
           );
  }

  /**
   * Converts a {@link ProductDto} to a {@link Product} entity.
   * <p>
   * This method maps the essential fields from the {@link ProductDto} to its corresponding entity.
   * The entity is used to interact with the database and perform CRUD operations.
   * </p>
   *
   * @param productDto The {@link ProductDto} to be mapped. Must not be {@code null}.
   * @return A {@link Product} entity containing the mapped fields from the provided {@link ProductDto}.
   * @throws NullPointerException if the provided productDto is {@code null}.
   */
  public static Product mapFromProductDtoToProduct(ProductDto productDto) {
    return new Product(
            productDto.productId(),
            productDto.productName(),
            productDto.productDescription(),
            productDto.productPrice(), //BigDecimal
            productDto.productQuantity(), //int
            productDto.productCategory(), //String
            productDto.availableStock(),
            productDto.expiryDate(),
            null,
            null
    );
  }

  /**
   * Converts a list of {@link Product} entities to a list of {@link ProductDto}.
   * <p>
   * This method maps the essential fields from the list of {@link Product} entities to their corresponding DTOs.
   * The DTOs are used to transfer data in a structured manner without exposing the entities directly.
   * </p>
   *
   * @param productList The list of {@link Product} entities to be mapped. Must not be {@code null}.
   * @return A list of {@link ProductDto} containing the mapped fields from the provided list of {@link Product} entities.
   * @throws NullPointerException if the provided productList is {@code null}.
   */
  public static List<ProductDto> mapFromProductListToProductDtoList(List<Product> productList) {
    return productList
            .stream()
            .map(ProductMapper::mapFromProductToProductDto)
            .toList();
  }

  /**
   * Converts a {@link CreateProductDto} to a {@link Product} entity.
   * <p>
   * This method maps the essential fields from the {@link CreateProductDto} to its corresponding entity.
   * The entity is used to interact with the database and perform CRUD operations.
   * </p>
   *
   * @param createProductRequestDto The {@link CreateProductDto} to be mapped. Must not be {@code null}.
   * @return A {@link Product} entity containing the mapped fields from the provided {@link CreateProductDto}.
   * @throws NullPointerException if the provided createProductRequestDto is {@code null}.
   */
  public static Product mapFromCreateProductDtoToProduct(CreateProductDto createProductRequestDto) {
    return new Product(
            null,
            createProductRequestDto.productName(),
            createProductRequestDto.productDescription(),
            createProductRequestDto.productPrice(), //BigDecimal
            createProductRequestDto.productQuantity(),
            createProductRequestDto.productCategory(),
            createProductRequestDto.availableStock(), //int
            createProductRequestDto.manufacturedDate(), //LocalDate
            createProductRequestDto.expiryDate(),//LocalDate
            null //updatedAt

           );
  }

  /**
   * Converts a {@link UpdateProductDto} to a {@link Product} entity.
   * <p>
   * This method maps the essential fields from the {@link UpdateProductDto} to its corresponding entity.
   * The entity is used to interact with the database and perform CRUD operations.
   * </p>
   *
   * @param updateProductRequestDto The {@link UpdateProductDto} to be mapped. Must not be {@code null}.
   * @return A {@link Product} entity containing the mapped fields from the provided {@link UpdateProductDto}.
   * @throws NullPointerException if the provided updateProductRequestDto is {@code null}.
   */
  public static Product mapFromUpdateProductDtoToProduct(UpdateProductDto updateProductRequestDto) {
    return new Product(
            null,
            updateProductRequestDto.productName(),
            updateProductRequestDto.productDescription(),
            updateProductRequestDto.productPrice(), //BigDecimal
            updateProductRequestDto.productQuantity(), //int
            updateProductRequestDto.productCategory(), //String
            updateProductRequestDto.availableStock(), //int
            null, //LocalDate
            null, //LocalDate
            null//LocalDate
            );
  }



}
