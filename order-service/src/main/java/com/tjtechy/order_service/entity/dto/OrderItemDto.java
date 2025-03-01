/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemDto {
//    private Long orderItemId;
   private UUID productId;
    private String productName;
    private Integer quantity;
    //private BigDecimal price;

    public OrderItemDto() {
    }

    public OrderItemDto(UUID productId, String productName, Integer quantity) {
    //    this.orderItemId = orderItemId;
      this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        //this.price = price;
    }

//    public Long getOrderItemId() {
//        return orderItemId;
//    }

//    public void setOrderItemId(Long orderItemId) {
//        this.orderItemId = orderItemId;
//    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

//    public BigDecimal getPrice() {
//        return price;
//    }
//
//    public void setPrice(BigDecimal price) {
//        this.price = price;
//    }

    @Override
    public String toString() {
        return "OrderItemDto{" +
//                "orderItemId=" + orderItemId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                //", price=" + price +
                '}';
    }

}


