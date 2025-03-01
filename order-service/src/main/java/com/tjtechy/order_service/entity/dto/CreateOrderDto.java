/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import java.util.List;

public class CreateOrderDto {

  private String customerName;
  private String customerEmail;
  private String shippingAddress;
  private List<OrderItemDto> orderItems;

  public CreateOrderDto() {
  }

  public CreateOrderDto(String customerName, String customerEmail, String shippingAddress, List<OrderItemDto> orderItems) {
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.shippingAddress = shippingAddress;
    this.orderItems = orderItems;
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
            ", shippingAddress='" + shippingAddress + '\'' +
            ", orderItems=" + orderItems +
            '}';
  }
}
