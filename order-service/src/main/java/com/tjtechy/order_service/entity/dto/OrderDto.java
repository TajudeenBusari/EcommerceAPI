/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderDto {
  private Long orderId;
  private String customerName;
  private String customerEmail;
  @Size(min = 7, max = 15, message = "Customer phone must be between 1 and 15 characters")
  @Pattern(
          regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
          message = "Invalid phone number format"
  )
  private String customerPhone;
  private String shippingAddress;
  private BigDecimal totalAmount; // Total amount of the order
  private LocalDate orderDate;
  private String orderStatus; // Order status (e.g., PLACED, SHIPPED, DELIVERED, CANCELLED)
  private List<OrderItemDto> orderItems; // List of products in the order

  public OrderDto() {
  }

  public OrderDto(Long orderId, String customerName, String customerEmail, String customerPhone, String shippingAddress, BigDecimal totalAmount, LocalDate orderDate, String orderStatus, List<OrderItemDto> orderItems) {
    this.orderId = orderId;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.customerPhone = customerPhone;
    this.shippingAddress = shippingAddress;
    this.totalAmount = totalAmount;
    this.orderDate = orderDate;
    this.orderStatus = orderStatus;
    this.orderItems = orderItems;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }
  public String getCustomerPhone() {
    return customerPhone;
  }
  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(String shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public String getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(String orderStatus) {
    this.orderStatus = orderStatus;
  }

  public List<OrderItemDto> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderItemDto> orderItems) {
    this.orderItems = orderItems;
  }

  @Override
  public String toString() {
    return "OrderDto{" +
            "orderId=" + orderId +
            ", customerName='" + customerName + '\'' +
            ", customerEmail='" + customerEmail + '\'' +
            "customerPhone='" + customerPhone + '\'' +
            ", shippingAddress='" + shippingAddress + '\'' +
            ", totalAmount=" + totalAmount +
            ", orderDate='" + orderDate + '\'' +
            ", orderStatus='" + orderStatus + '\'' +
            ", orderItems=" + orderItems +
            '}';
  }

}
