/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.service.impl;

import businessException.InsufficientStockQuantityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.tjtechy.order_service.config.ProductServiceConfig;
import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.repository.OrderRepository;
import com.tjtechy.order_service.service.OrderService;
import com.tjtechy.product_service.entity.dto.ProductDto;
import com.tjtechy.system.Result;
import jakarta.transaction.Transactional;
import modelNotFound.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  private final WebClient.Builder webClientBuilder;

  //private ProductServiceConfig productServiceConfig;

  public OrderServiceImpl(OrderRepository orderRepository, WebClient.Builder webClientBuilder) {
    this.orderRepository = orderRepository;
    this.webClientBuilder = webClientBuilder;
    //this.productServiceConfig = productServiceConfig;
  }


  /**
   * Use Transactional context to ensure that the operation is atomic
   * For example, if the order creation fails, the transaction will be rolled back
   * A good example is if saving fails after deduction from Inventory, the
   * Inventory deduction should be reversed.
   * @param order
   * @return
   */
  @Transactional
  @Override
  public Order createOrder(Order order) {

    //1. Validate order
    /**Throw the IllegalArgumentException if the order is invalid
     * The MethodArgumentNotValidException does not need to be
     * stated here because it is handled by the controller with
     * @Valid annotation
     */
    if (order == null || order.getOrderItems().isEmpty() || order.getOrderItems() == null) {
      throw new IllegalArgumentException("Order details are not complete");
    }


    BigDecimal totalAmount = BigDecimal.ZERO;

    //2. Retrieve product details and validate stock
    for(var orderItem : order.getOrderItems()) {
      var productId = orderItem.getProductId();
      var quantity = orderItem.getProductQuantity();

      //a. Call product service to get product details
      var productResponse = webClientBuilder.build()
              .get()
              .uri("http://localhost:8083/api/v1/product/" + productId)//Using service name from Eureka
              //.uri("http://product-service" + productServiceConfig.getBaseUrl() + "/product/" + productId)//Using service name from Eureka
              //.uri("http://product-service${api.endpoint.base-url}/product/" + productId)//Using service name from Eureka
              .retrieve()
              .bodyToMono(Result.class)
              .block(); //block to wait for the response, can be replaced with async

      if (productResponse == null || productResponse.getData() == null) {
        throw new ProductNotFoundException(productId);
      }

      // Convert LinkedHashMap to ProductDto manually
      var objectMapper = new ObjectMapper(); //Jackson ObjectMapper

      //Register JavaTimeModule to handle LocalDateTime serialization/deserialization
      objectMapper.registerModule(new JavaTimeModule());

      //This will correctly convert the LinkedHashMap into a ProductDto object.
      ProductDto productDto = objectMapper.convertValue(productResponse.getData(), ProductDto.class);

      if (productDto == null) {
        throw new ProductNotFoundException(productId);
      }


      //b. check if product quantity is available
      if (productDto.productQuantity() < quantity) {
        throw new InsufficientStockQuantityException(productId);
      }

      //c update order item with product details
      orderItem.setProductName(productDto.productName());
      orderItem.setProductPrice(productDto.productPrice());

      //d. calculate the subtotal for the order item
      var itemTotal = productDto.productPrice().multiply(BigDecimal.valueOf(quantity));
      totalAmount = totalAmount.add(itemTotal);


      System.out.println("Product Price: " + itemTotal);
      System.out.println("Product Quantity: " + quantity);

      /**
       * set the order for the order item
       * //DONE: This logic is already been implemented in the order class
       *  //orderItem.setOrder(order);
       *
       */
      //this method from the order class to add orderItem to order
      order.addOrderItems(order.getOrderItems());
    }

    order.setTotalAmount(totalAmount);
    System.out.println("Final Total Amount: " + order.getTotalAmount());

    //3. Deduct from Inventory
    //ToDo: Implement Inventory deduction
    //inventoryService.deductStockFromInventory(order.getOrderItems());

    //4. Set order status to "PLACED"
    order.setOrderStatus("PLACED");

    //5. return Saved order
    return orderRepository.save(order);

  }

  @Override
  public Order getOrderById(Long orderId) {
    return null;
  }

  @Override
  public List<Order> getAllOrders() {
    return List.of();
  }

  @Override
  public List<Order> getOrdersByCustomerEmail(String customerEmail) {
    return List.of();
  }

  @Override
  public List<Order> getOrdersByStatus(String orderStatus) {
    return List.of();
  }

  @Override
  public Order updateOrderStatus(Long orderId, String orderStatus) {
    return null;
  }

  @Override
  public Order updateOrder(Long orderId, Order order) {
    return null;
  }

  @Override
  public void deleteOrder(Long orderId) {

  }

  @Override
  public void cancelOrder(Long orderId) {

  }

  @Override
  public void clearAllCache() {

  }
}
