/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.controller;

import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.UpdateOrderDto;
import com.tjtechy.order_service.mapper.OrderMapper;
import com.tjtechy.order_service.service.OrderService;
import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${api.endpoint.base-url}/order")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;}


  /**
   * This is the method to create order
   * Non-reactive method, blocking and synchronous
   * The entire request thread is held until processing completes.
   * If external services (like a Product Service or Database) take longer,
   * the request hangs until it’s done.
   * Cannot efficiently handle multiple external service calls
   * (e.g., calling product service for each item in the order) and will be
   * deprecated in the next version.
   * @param createOrderDto
   * @return
   */
  @PostMapping
  @Deprecated(since = "1.1", forRemoval = true)
  public Result createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
    //map from createOrderDto to Order
    var order = OrderMapper.mapFromCreateOrderDtoToOrder(createOrderDto);

    //call orderService.createOrder
    var createdOrder = orderService.createOrder(order);

    //map from Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(createdOrder);

    return new Result("Order created successfully", true, orderDto, StatusCode.SUCCESS);
  }

  /**
   * This is the method to create order reactively
   * The Mono means that the result will be returned asynchronously
   * Reactive is non-blocking and asynchronous which improves performance.
   * Efficient when calling external services or APIs, E.g., product service etc.
   * Returns a reactive stream, freeing up the thread to handle other requests.
   * The createOrder is processed inside the map method.
   * @param createOrderDto
   * @return
   */
  @PostMapping("/reactive")
  public Mono<Result> processOrderReactively(@Valid @RequestBody CreateOrderDto createOrderDto) {
    //map from createOrderDto to Order
    var order = OrderMapper.mapFromCreateOrderDtoToOrder(createOrderDto);

    //call orderService.createOrder
    return orderService.processOrderReactively(order)
            .map(createdOrder -> {
              //map from Order to OrderDto
              var orderDto = OrderMapper.mapFromOrderToOrderDto(createdOrder);

              return new Result("Order created successfully", true, orderDto, StatusCode.SUCCESS);
            });
  }

  /**
   * This is deprecated and will be removed in the next version
   * because caching logic is not implemented as result of serialization issues
   * Use {@link #getOrderDtoById(Long)} instead
   * @param orderId
   * @return
   */
  @Deprecated(since = "1.1", forRemoval = true)
  @GetMapping("/{orderId}")
  public Result getOrderById(@PathVariable Long orderId) {

    var order = orderService.getOrderById(orderId);

    //map from Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(order);

    return new Result("Order retrieved successfully", true, orderDto, StatusCode.SUCCESS);
  }

  /**
   * This is the new method to get order by id
   * @param orderId
   * @return
   */
  @GetMapping("orderDto/{orderId}")
  public Result getOrderDtoById(@PathVariable Long orderId) {

    var orderDto = orderService.getOrderDtoById(orderId);

    return new Result("OrderDto retrieved successfully", true, orderDto, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get all orders
   * @return
   */
  @GetMapping
  public Result getAllOrders() {

    var orderDtoList = orderService.getAllOrders();

    return new Result("Orders retrieved successfully", true, orderDtoList, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get orders by customer email
   * @param customerEmail
   * @return
   */
  @GetMapping("/customer")
  public Result getOrdersByCustomerEmail(@RequestParam String customerEmail) {

    var orderDtos = orderService.getOrdersByCustomerEmail(customerEmail);

    //map from List<Order> to List<OrderDto>
    //var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders by email retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to delete order
   * @param orderId
   * @return
   */
  @DeleteMapping("/{orderId}")
  public Result deleteOrder(@PathVariable Long orderId) {
    //call orderService.deleteOrder
    orderService.deleteOrder(orderId);

    return new Result("Order deleted successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get all orders without cancelled ones
   * @return
   */
  @GetMapping("/without-cancelled")
  public Result getAllOrdersWithoutCancelledOnes() {

    var orderDtos = orderService.getAllOrdersWithoutCancelledOnes();

    //map from List<Order> to List<OrderDto>
    //var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to cancel order
   * @param orderId
   * @return
   */
  @DeleteMapping("/cancel/{orderId}")
  public Result cancelOrder(@PathVariable Long orderId) {

    orderService.cancelOrder(orderId);

    return new Result("Order cancelled successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get orders by status
   * @param orderStatus
   * @return
   */
  @GetMapping("/status")
  public Result getOrdersByStatus(@RequestParam String orderStatus) {

    var orderDtos = orderService.getOrdersByStatus(orderStatus);

    //map from List<Order> to List<OrderDto>
    //var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders by status retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to update order status
   * @param orderId
   * @param orderStatus
   * @return
   */
  @PutMapping("/{orderId}/update-status")
  public Result updateOrderStatus(@PathVariable Long orderId, @RequestParam String orderStatus) {

    //check if order status is valid
    var order = new Order();
    if (!order.isOrderStatusValid(orderStatus)) {
      return new Result("Invalid order status", false, StatusCode.BAD_REQUEST);
    }

    var orderWithNewStatus = orderService.updateOrderStatus(orderId, orderStatus);
    //map from Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(orderWithNewStatus);

    return new Result("Order status updated successfully", true, orderDto, StatusCode.SUCCESS);
  }

  /**
   * This is the method to update order
   * @param orderId
   * @param updateOrderDto
   * @return
   */
  @PutMapping("/{orderId}")
  public Mono<Result> updateOrder(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderDto updateOrderDto) {


    var order = OrderMapper.mapFromUpdateOrderDtoToOrder(updateOrderDto);

    //call orderService.updateOrder
    return orderService.updateOrder(orderId, order)
            .map(updatedOrder -> {
              //map from Order to OrderDto
              var orderDto = OrderMapper.mapFromOrderToOrderDto(updatedOrder);

              return new Result("Order updated successfully", true, orderDto, StatusCode.SUCCESS);
            });
  }

  /**
   * This is the method to clear all cache
   * @return
   */
  @DeleteMapping("/clear-cache")
  public Result clearCache() {
    orderService.clearAllCache();
    return new Result("Cache cleared successfully", true, null, StatusCode.SUCCESS);
  }


}
