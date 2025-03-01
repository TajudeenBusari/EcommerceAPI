/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.mapper;

import com.tjtechy.order_service.entity.OrderItem;
import com.tjtechy.order_service.entity.dto.OrderItemDto;

import java.util.List;

public class OrderItemMapper {
  public static OrderItemDto mapFromOrderItemToOrderItemDto(OrderItem orderItem) {
    var orderItemDto = new OrderItemDto();
//    orderItemDto.setOrderItemId(orderItem.getOrderItemId());
   orderItemDto.setProductId(orderItem.getProductId());
    orderItemDto.setProductName(orderItem.getProductName());
    orderItemDto.setQuantity(orderItem.getProductQuantity());
    //orderItemDto.setPrice(orderItem.getProductPrice());
    return orderItemDto;
  }

  public static OrderItem mapFromOrderItemDtoToOrderItem(OrderItemDto orderItemDto) {
    var orderItem = new OrderItem();
//    orderItem.setOrderItemId(orderItemDto.getOrderItemId());
   orderItem.setProductId(orderItemDto.getProductId());
    orderItem.setProductName(orderItemDto.getProductName());
    orderItem.setProductQuantity(orderItemDto.getQuantity());
    //orderItem.setProductPrice(orderItemDto.getPrice());
    return orderItem;
  }

  public static List<OrderItem> mapFromOrderItemDtosToOrderItems(List<OrderItemDto> orderItemDtos) {
    return orderItemDtos.stream().map(OrderItemMapper::mapFromOrderItemDtoToOrderItem).toList();
  }

  public static List<OrderItemDto> mapFromOrderItemsToOrderItemDtos(List<OrderItem> orderItems) {
    return orderItems.stream().map(OrderItemMapper::mapFromOrderItemToOrderItemDto).toList();
  }
}
