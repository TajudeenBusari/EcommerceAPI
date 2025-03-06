package com.tjtechy.order_service.controller;

import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.mapper.OrderMapper;
import com.tjtechy.order_service.service.OrderService;
import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.base-url}/order")
public class OderController {
  private final OrderService orderService;

  public OderController(OrderService orderService) {
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


}
