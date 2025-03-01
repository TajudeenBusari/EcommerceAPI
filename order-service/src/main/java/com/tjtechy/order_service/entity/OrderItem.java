/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * This class establishes the relationship between the Order and Product entities.
 */
@Entity
@Table(name = "order_items")
public class OrderItem implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long orderItemId;

  @Column(nullable = false)
  private UUID productId; // Reference to the product ID from the Product Service

  @Column(nullable = false)
  private String productName;

  @Column(nullable = false)
  private BigDecimal productPrice;

  @Column(nullable = false)
  private Integer productQuantity;

  /**
   * Relationship with Order via OrderItem (composition)
   * The many side of the relationship is OrderItem
   * The one side of the relationship is Order
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  public OrderItem() {
  }

  public Long getOrderItemId() {
    return orderItemId;
  }

  public void setOrderItemId(Long orderItemId) {
    this.orderItemId = orderItemId;
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

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "OrderItem{" +
            "orderItemId=" + orderItemId +
            ", productId=" + productId +
            ", productName='" + productName + '\'' +
            ", productPrice=" + productPrice +
            ", productQuantity=" + productQuantity +
            '}';
  }

}
