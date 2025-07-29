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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;

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
  @Operation(summary = "Create a new order",
  description = "This endpoint creates a new order based on the provided CreateOrderDto. " +
          "Endpoint is deprecated and will be removed in the next version. Use any of the /reactive instead.",
          responses = {@ApiResponse(responseCode = "200", description = "Order created successfully"),

          }

  )
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
  @Operation(summary = "Create a new order reactively",
          description = "This endpoint creates a new order reactively based on the provided CreateOrderDto. " +
                  "It is non-blocking and asynchronous, " +
                  "improving performance when calling external services or APIs.",
  responses = {@ApiResponse(responseCode = "200", description = "Order created successfully")
  })
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
   * This is the method to create order reactively by calling externalized services
   * The Mono means that the result will be returned asynchronously
   * Reactive is non-blocking and asynchronous which improves performance.
   * Efficient when calling external services or APIs, E.g., product service etc.
   * Returns a reactive stream, freeing up the thread to handle other requests.
   * The createOrder is processed inside the map method.
   * @param createOrderDto
   * @return
   */
  @Operation(summary = "Create a new order reactively by calling externalized services",
          description = "This endpoint creates a new order reactively by calling externalized services" +
                  ". It is non-blocking and asynchronous",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order created successfully by calling required external services")
  })
  @PostMapping("/reactive/externalized")
  public Mono<Result> processOrderReactivelyByCallingExternalizedServices(@Valid @RequestBody CreateOrderDto createOrderDto) {
    //map from createOrderDto to Order
    var order = OrderMapper.mapFromCreateOrderDtoToOrder(createOrderDto);

    //call orderService.processOrderReactivelyByCallingExternalizedServices
    return orderService.processOrderReactivelyByCallingExternalizedServices(order)
            .map(createdOrder -> {
              //map from Order to OrderDto
              var orderDto = OrderMapper.mapFromOrderToOrderDto(createdOrder);

              return new Result("Order created successfully by calling required external services", true, orderDto, StatusCode.SUCCESS);
            });
  }


  /**
   * This is deprecated and will be removed in the next version
   * because caching logic is not implemented as result of serialization issues
   * Use {@link #getOrderDtoById(Long)} instead
   * @param orderId
   * @return
   */
  @Operation(summary = "Get order by ID",
          description = "This endpoint retrieves an order by its ID. " +
                  "It is deprecated and will be removed in the next version. Use /orderDto/{orderId} instead.")
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
  @Operation(summary = "Get order by ID",
          description = "This endpoint retrieves an order by its ID. " +
                  "It is the preferred method to use instead of the deprecated /{orderId} endpoint.",
  responses = {@ApiResponse(responseCode = "200", description = "OrderDto retrieved successfully")
  })
  @GetMapping("orderDto/{orderId}")
  public Result getOrderDtoById(@PathVariable Long orderId) {

    var orderDto = orderService.getOrderDtoById(orderId);

    return new Result("OrderDto retrieved successfully", true, orderDto, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get all orders
   * @return
   */
  @Operation(summary = "Get all orders",
          description = "This endpoint retrieves all orders in the system.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
  })
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
  @Operation(summary = "Get orders by customer email",
          description = "This endpoint retrieves all orders associated with a specific customer email.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Orders by email retrieved successfully")
  })
  @GetMapping("/customer")
  public Result getOrdersByCustomerEmail(@RequestParam String customerEmail) {

    var orderDtos = orderService.getOrdersByCustomerEmail(customerEmail);


    return new Result("Orders by email retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to delete order
   * @param orderId
   * @return
   */
  @Operation(summary = "Delete an order by ID",
          description = "This endpoint deletes an order by its ID.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order deleted successfully")
  })
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
  @Operation(summary = "Get all orders without cancelled ones",
          description = "This endpoint retrieves all orders excluding those that are cancelled.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Orders retrieved successfully without cancelled ones")
  })
  @GetMapping("/without-cancelled")
  public Result getAllOrdersWithoutCancelledOnes() {

    var orderDtos = orderService.getAllOrdersWithoutCancelledOnes();

    return new Result("Orders retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to cancel order
   * @param orderId
   * @return
   */
  @Operation(summary = "Cancel an order by ID",
          description = "This endpoint cancels an order by its ID.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
  })
  @DeleteMapping("/cancel/{orderId}")
  public Result cancelOrder(@PathVariable Long orderId) {

    orderService.cancelOrder(orderId);

    return new Result("Order cancelled successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This is the method to bulk delete orders
   * @param orderIds
   * @return
   */
  @Operation(summary = "Bulk delete orders",
          description = "This endpoint allows bulk deletion of orders based on a list of order IDs.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Orders bulk deleted successfully")
  })
  @DeleteMapping("/bulk-delete")
  public Result bulkDeleteOrders(@RequestBody List<Long> orderIds) {

    orderService.bulkDeleteOrders(orderIds);

    return new Result("Orders bulk deleted success", true, null, StatusCode.SUCCESS);
  }

  /**
   * This is the method to get orders by status
   * @param orderStatus
   * @return
   */
  @Operation(summary = "Get orders by status",
          description = "This endpoint retrieves all orders with a specific status.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Orders by status retrieved successfully")
  })
  @GetMapping("/status")
  public Result getOrdersByStatus(@RequestParam String orderStatus) {

    var orderDtos = orderService.getOrdersByStatus(orderStatus);

    return new Result("Orders by status retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  /**
   * This is the method to update order status
   * @param orderId
   * @param orderStatus
   * @return
   */
  @Operation(summary = "Update order status",
          description = "This endpoint updates the status of an order by its ID.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order status updated successfully")
  })
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
  @Operation(summary = "Update an order",
          description = "This endpoint updates an existing order based on the provided UpdateOrderDto.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order updated successfully")
  })
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

  @Operation(summary = "Update an order by calling externalized services",
          description = "This endpoint updates an existing order by calling externalized services based on the provided UpdateOrderDto.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Order updated successfully by calling required external services")
  })
  @PutMapping("/externalized/{orderId}")
  public Mono<Result> updateOrderByCallingExternalizedServices(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderDto updateOrderDto) {

    var order = OrderMapper.mapFromUpdateOrderDtoToOrder(updateOrderDto);

    //call orderService.updateOrderByCallingExternalizedServices
    return orderService.updateOrderByCallingExternalizedServices(orderId, order)
            .map(updatedOrder -> {
              //map from Order to OrderDto
              var orderDto = OrderMapper.mapFromOrderToOrderDto(updatedOrder);

              return new Result("Order updated successfully by calling required external services", true, orderDto, StatusCode.SUCCESS);
            });
  }

  /**
   * This is the method to clear all cache
   * @return
   */
  @Operation(summary = "Clear all cache",
          description = "This endpoint clears all cached data related to orders.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
  })
  @DeleteMapping("/clear-cache")
  public Result clearCache() {
    orderService.clearAllCache();
    return new Result("Cache cleared successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This is the method to force delete order
   * This method is strictly for Admin use only
   * for database cleanup or emergency situations
   * ToDo: In the future, it will be secured, only accessible by Admin users and
   * the name and timestamp of the user who performed the action will be logged
   * Bulk delete will not be implemented for this method to discourage misuse
   *
   * @param orderId
   * @return
   */
  @DeleteMapping
  ("/forced-delete/{orderId}")
  public Result forcedDeleteOrder(@PathVariable Long orderId) {
    orderService.forcedDeleteOrder(orderId);
    return new Result("Order forced deleted successfully", true, null, StatusCode.SUCCESS);
  }


}
