package com.tjtechy.product_service.service;

import com.tjtechy.product_service.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
  List<Product> getAllProducts();
  Product getProductById(UUID productId);
  Product saveProduct(Product product);
  Product saveProductWithInventory(Product product);
  Product saveProductWithInventoryUsingExternalizedService(Product product);
  Product updateProduct(UUID productId, Product product);
  Product updateProductWithInventory(UUID productId, Product product);
  Product updateProductWithInventoryUsingExternalizedService(UUID productId, Product product);
  void deleteProduct(UUID productId);
  void clearAllCache();
  void bulkDeleteProducts(List<UUID> productIds);
  void deleteProductWithInventory(UUID productId);
  void deleteProductWithInventoryUsingExternalizedService(UUID productId);
  void bulkDeleteProductsWithInventories(List<UUID> productIds);
  void bulkDeleteProductsWithInventoriesUsingExternalizedService(List<UUID> productIds);


}
