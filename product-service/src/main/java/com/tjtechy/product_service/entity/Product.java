/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "products")
/**
 *int is a primitive type and cannot be null, so we use Integer
 * Integer is an object wrapper for int and can be null.
 */
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;
    @Column(nullable = false, unique = true)
    private String productName;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String productDescription;
    @Column(nullable = false)
    private BigDecimal productPrice;
    @Column(nullable = false)
    /**
     * The difference between productQuantity and availableStock is that:
     * productQuantity is the total quantity of the product that was delivered to the store.
     * availableStock is the quantity of the product that is currently available
     * for sale in the store. Some product may not be available for sale, for
     * example, due to spoilage, damage, or other reasons.
     * In future update, availableStock will be changed to initialStock.
     * It will be compared to the availableStock in the inventory service
     * to know the current stock level of the product.
     */
    private Integer productQuantity;
    @Column(nullable = false)
    private String productCategory;
    @Column(nullable = false)
    private Integer availableStock; // Related to Inventory Service //TODO: Change to initialStock in future update.
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expiryDate; //Expiry date for perishable products.
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate manufacturedDate; //Timestamp when the product was created.
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate updatedAt; //Timestamp for the last update.

  @PrePersist // Before saving the product to the database, set the manufactured date and updated date to the current date.
  protected void onCreate() {
    this.manufacturedDate = LocalDate.now();
    this.updatedAt = LocalDate.now();
  }

  @PreUpdate // Before updating the product in the database, set the updated date to the current date.
  protected void onUpdate() {
    this.updatedAt = LocalDate.now();
  }

    public Product() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDate getManufacturedDate() {
        return manufacturedDate;
    }

    public void setManufacturedDate(LocalDate manufacturedDate) {
        this.manufacturedDate = manufacturedDate;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Product(UUID productId, String productName, String productDescription, BigDecimal productPrice, int productQuantity, String productCategory, int availableStock, LocalDate expiryDate, LocalDate manufacturedDate, LocalDate updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.productCategory = productCategory;
        this.availableStock = availableStock;
        this.expiryDate = expiryDate;
        this.manufacturedDate = manufacturedDate;
        this.updatedAt = updatedAt;
  }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", productPrice=" + productPrice +
                ", productQuantity=" + productQuantity +
                ", productCategory='" + productCategory + '\'' +
                ", availableStock=" + availableStock +
                ", expiryDate=" + expiryDate +
                ", manufacturedDate=" + manufacturedDate +
                ", updatedAt=" + updatedAt +
                '}';
    }



}
