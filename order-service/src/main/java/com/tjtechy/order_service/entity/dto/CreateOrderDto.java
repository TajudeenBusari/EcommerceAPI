/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOrderDto {

  private String customerName;
  private String customerEmail;
  @Size(min = 7, max = 15, message = "Customer phone must be between 7 and 15 characters")
  @Pattern(
          regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
          message = "Invalid phone number format"
  )
  private String customerPhone;
  private String shippingAddress;
  private List<OrderItemDto> orderItems;

  public CreateOrderDto() {
  }

  public CreateOrderDto(String customerName, String customerEmail, String customerPhone, String shippingAddress, List<OrderItemDto> orderItems) {
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.shippingAddress = shippingAddress;
    this.orderItems = orderItems;
    this.customerPhone = customerPhone;
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

  public String getCustomerPhone() {
    return customerPhone;
  }
  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(String shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public List<OrderItemDto> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderItemDto> orderItems) {
    this.orderItems = orderItems;
  }

  @Override
  public String toString() {
    return "CreateOrderDto{" +
            "customerName='" + customerName + '\'' +
            ", customerEmail='" + customerEmail + '\'' +
            "customerPhone='" + customerPhone + '\'' +
            ", shippingAddress='" + shippingAddress + '\'' +
            ", orderItems=" + orderItems +
            '}';
  }
}
