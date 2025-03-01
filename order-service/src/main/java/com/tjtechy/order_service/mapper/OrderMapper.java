/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.mapper;

import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderDto;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {
  public static OrderDto mapFromOrderToOrderDto(Order order) {
    var orderDto = new OrderDto();
    orderDto.setOrderId(order.getOrderId());
    orderDto.setOrderDate(order.getOrderDate());
    orderDto.setOrderStatus(order.getOrderStatus());
    orderDto.setCustomerName(order.getCustomerName());
    orderDto.setCustomerEmail(order.getCustomerEmail());
    orderDto.setShippingAddress(order.getShippingAddress());
    orderDto.setTotalAmount(order.getTotalAmount());
    orderDto.setCustomerEmail(order.getCustomerEmail());
    orderDto.setOrderItems(OrderItemMapper.mapFromOrderItemsToOrderItemDtos(order.getOrderItems())); //from OrderItemMapper

    return orderDto;
  }

  public static List<OrderDto> mapFromOrdersToOrderDtos(List<Order> orders) {
    return orders.stream().map(OrderMapper::mapFromOrderToOrderDto).collect(Collectors.toList());
  }

  public static Order mapFromCreateOrderDtoToOrder(CreateOrderDto createOrderDto) {
    var order = new Order();

    order.setCustomerName(createOrderDto.getCustomerName());
    order.setCustomerEmail(createOrderDto.getCustomerEmail());
    order.setShippingAddress(createOrderDto.getShippingAddress());
    order.setOrderItems(OrderItemMapper.mapFromOrderItemDtosToOrderItems(createOrderDto.getOrderItems())); //from OrderItemMapper

    return order;
  }
}
