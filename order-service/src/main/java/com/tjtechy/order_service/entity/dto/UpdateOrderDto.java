/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity.dto;

import java.util.List;

public class UpdateOrderDto {

  private String customerName;
  private String customerEmail;
  private String shippingAddress;
  private List<OrderItemDto> orderItems;
}
