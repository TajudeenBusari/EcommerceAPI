package com.tjtechy.order_service.controller;

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


  @PostMapping
  public Result createOrder(@Valid @RequestBody CreateOrderDto createOrderDto) {
    //map from createOrderDto to Order
    var order = OrderMapper.mapFromCreateOrderDtoToOrder(createOrderDto);

    //call orderService.createOrder
    var createdOrder = orderService.createOrder(order);

    //map from Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(createdOrder);

    return new Result("Order created successfully", true, orderDto, StatusCode.SUCCESS);
  }

  @GetMapping("/{orderId}")
  public Result getOrderById(@PathVariable Long orderId) {
    //call orderService.getOrderById
    var order = orderService.getOrderById(orderId);

    //map from Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(order);

    return new Result("Order retrieved successfully", true, orderDto, StatusCode.SUCCESS);
  }

  @GetMapping
  public Result getAllOrders() {
    //call orderService.getAllOrders
    var orders = orderService.getAllOrders();

    //map from List<Order> to List<OrderDto>
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  @GetMapping("/customer")
  public Result getOrdersByCustomerEmail(@RequestParam String customerEmail) {

    //call orderService.getOrdersByCustomerEmail
    var orders = orderService.getOrdersByCustomerEmail(customerEmail);

    //map from List<Order> to List<OrderDto>
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders by email retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  @DeleteMapping("/{orderId}")
  public Result deleteOrder(@PathVariable Long orderId) {
    //call orderService.deleteOrder
    orderService.deleteOrder(orderId);

    return new Result("Order deleted successfully", true, null, StatusCode.SUCCESS);
  }

  @GetMapping("/without-cancelled")
  public Result getAllOrdersWithoutCancelledOnes() {
    //call orderService.getAllOrdersWithoutCancelledOnes
    var orders = orderService.getAllOrdersWithoutCancelledOnes();

    //map from List<Order> to List<OrderDto>
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  @DeleteMapping("/cancel/{orderId}")
  public Result cancelOrder(@PathVariable Long orderId) {
    //call orderService.cancelOrder
    orderService.cancelOrder(orderId);

    return new Result("Order cancelled successfully", true, null, StatusCode.SUCCESS);
  }

  @GetMapping("/status")
  public Result getOrdersByStatus(@RequestParam String orderStatus) {
    //call orderService.getOrdersByStatus
    var orders = orderService.getOrdersByStatus(orderStatus);

    //map from List<Order> to List<OrderDto>
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orders);

    return new Result("Orders by status retrieved successfully", true, orderDtos, StatusCode.SUCCESS);
  }

  @PutMapping("/{orderId}/update-status")
  public Result updateOrderStatus(@PathVariable Long orderId, @RequestParam String orderStatus) {
    //call orderService.updateOrderStatus
    orderService.updateOrderStatus(orderId, orderStatus);

    return new Result("Order status updated successfully", true, null, StatusCode.SUCCESS);
  }

  @PutMapping("/{orderId}")
  public Mono<Result> updateOrder(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderDto updateOrderDto) {

    //map from UpdateOrderDto to Order
    var order = OrderMapper.mapFromUpdateOrderDtoToOrder(updateOrderDto);

    //call orderService.updateOrder
    return orderService.updateOrder(orderId, order)
            .map(updatedOrder -> {
              //map from Order to OrderDto
              var orderDto = OrderMapper.mapFromOrderToOrderDto(updatedOrder);

              return new Result("Order updated successfully", true, orderDto, StatusCode.SUCCESS);
            });

  }

}
