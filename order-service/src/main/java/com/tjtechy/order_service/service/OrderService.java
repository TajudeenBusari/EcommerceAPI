package com.tjtechy.order_service.service;

import com.tjtechy.order_service.entity.Order;

import java.util.List;

public interface OrderService {
  Order createOrder(Order order);
  Order getOrderById(Long orderId);
  List<Order> getAllOrders();

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
  List<Order>  getOrdersByCustomerEmail(String customerEmail);

  List<Order> getOrdersByStatus(String orderStatus);

  Order updateOrderStatus(Long orderId, String orderStatus);

  Order updateOrder(Long orderId, Order order);

  void deleteOrder(Long orderId);

  void cancelOrder(Long orderId);

  void clearAllCache();

}
