/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.service;

import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.dto.OrderDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
  Order createOrder(Order order);
  Mono<Order> processOrderReactively(Order order);
  Mono<Order> processOrderReactivelyByCallingExternalizedServices(Order order);
  Order getOrderById(Long orderId);
  List<OrderDto> getAllOrders();
  List<OrderDto> getAllOrdersWithoutCancelledOnes();

  /**
   * Get all orders by customer email and status
   * A customer can have multiple orders, placed at different times.
   * Returning a list of orders provides more flexibility for
   * future enhancements. You might want to show customer's order history
   * You might need to filter or paginate the results based on additional
   * criteria (e.g., date range, status etc.)
   *  Handling Edge Cases
   * If no orders exist for the specified customerEmail, an empty list ([]) can be
   * returned instead of throwing an exception or returning null.
   * This avoids unnecessary complexity in error handling.
   * If only one order exists for the customer, the list will simply contain a single element.
   * @param customerEmail
   * @return
   */
  List<OrderDto>  getOrdersByCustomerEmail(String customerEmail);

  List<OrderDto> getOrdersByStatus(String orderStatus);

  Order updateOrderStatus(Long orderId, String orderStatus);

  Mono<Order> updateOrder(Long orderId, Order order);
  Mono<Order> updateOrderByCallingExternalizedServices(Long orderId, Order order);

  void deleteOrder(Long orderId);

  void bulkDeleteOrders(List<Long> orderIds);

  void cancelOrder(Long orderId);

  void clearAllCache();

  OrderDto getOrderDtoById(Long orderId);

  //strictly for Admin use only
  void forcedDeleteOrder(Long orderId);



}
