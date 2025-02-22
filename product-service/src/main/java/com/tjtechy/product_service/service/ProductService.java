package com.tjtechy.product_service.service;

import com.tjtechy.product_service.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
  List<Product> getAllProducts();
  Product getProductById(UUID productId);
  Product saveProduct(Product product);
  Product updateProduct(UUID productId, Product product);
  void deleteProduct(UUID productId);
  void clearAllCache();
}
