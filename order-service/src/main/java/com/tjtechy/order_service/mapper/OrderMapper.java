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
import com.tjtechy.order_service.entity.dto.UpdateOrderDto;
import jakarta.validation.Valid;

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
    orderDto.setCustomerPhone(order.getCustomerPhone());
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
    order.setCustomerPhone(createOrderDto.getCustomerPhone());
    order.setShippingAddress(createOrderDto.getShippingAddress());
    order.setOrderItems(OrderItemMapper.mapFromOrderItemDtosToOrderItems(createOrderDto.getOrderItems())); //from OrderItemMapper

    return order;
  }

  public static Order mapFromUpdateOrderDtoToOrder(UpdateOrderDto updateOrderDto) {
    var order = new Order();

    order.setCustomerName(updateOrderDto.customerName());
    order.setCustomerEmail(updateOrderDto.customerEmail());
    order.setCustomerPhone(updateOrderDto.customerPhone());
    order.setShippingAddress(updateOrderDto.shippingAddress());
    order.setOrderItems(OrderItemMapper.mapFromOrderItemDtosToOrderItems(updateOrderDto.orderItems())); //from OrderItemMapper

    return order;
  }
}
