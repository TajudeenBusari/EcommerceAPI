/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.service.impl;


import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tjtechy.modelNotFoundException.OrderNotFoundException;
import com.tjtechy.order_service.config.ProductServiceConfig;
import com.tjtechy.order_service.entity.Order;

import com.tjtechy.order_service.repository.OrderRepository;
import com.tjtechy.order_service.service.OrderService;
import com.tjtechy.Result;

import com.tjtechy.ProductDto;
import jakarta.transaction.Transactional;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  private final WebClient.Builder webClientBuilder;

  private final ProductServiceConfig productServiceConfig;

  public OrderServiceImpl(OrderRepository orderRepository, WebClient.Builder webClientBuilder, ProductServiceConfig productServiceConfig) {
    this.orderRepository = orderRepository;
    this.webClientBuilder = webClientBuilder;
    this.productServiceConfig = productServiceConfig;
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
  @CachePut(value = "order", key = "#order.orderId")
  public Order createOrder(Order order) {

    //ToDo: Implement Mono or Flux for reactive programming for better performance
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
              //.uri("http://localhost:8083/api/v1/product/" + productId)//Using service name from Eureka discovery
              .uri("http://product-service" + productServiceConfig.getBaseUrl() + "/product/" + productId)//Using service name from Eureka
              //.uri("http://product-service${api.endpoint.base-url}/product/" + productId)//Using service name from Eureka
              .retrieve()
              .bodyToMono(Result.class)
              .block(); //block to wait for the response, can be replaced with async;


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

  /**
   * Retrieve an order by its ID
   * @param orderId
   * @return
   */
  @Override
  public Order getOrderById(Long orderId) {

    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    return foundOrder;

  }

  /**
   * Retrieve all orders
   * @return
   */
  @Override
  public List<Order> getAllOrders() {

    return orderRepository.findAll();
  }

  @Override
  public List<Order> getAllOrdersWithoutCancelledOnes() {
    return orderRepository.findByOrderStatusNot("CANCELLED");
  }

  /**The method findOrderByEmail is a custom method created in the
   * OrderRepository interface
   * This method Retrieves all orders by customer email
   * @param customerEmail
   * @return
   */
  @Override
  public List<Order> getOrdersByCustomerEmail(String customerEmail) {
    if (customerEmail == null || customerEmail.isEmpty()) {
      throw new IllegalArgumentException("Customer email is required");
    }
    var orders = orderRepository.findByCustomerEmail(customerEmail);
    return orders;
    //TODO: Implement pagination and filtering
  }

  @Override
  public List<Order> getOrdersByStatus(String orderStatus) {
    if (orderStatus == null || orderStatus.isBlank()) {
      throw new IllegalArgumentException("Order status is required");
    }

    //trim and convert to uppercase
    orderStatus = orderStatus.trim().toUpperCase();

    //Use order class method to validate order status
    var orderToBeValidated = new Order();

    //Validate order status against allowed statuses
    if(!orderToBeValidated.isOrderStatusValid(orderStatus)){
      throw new IllegalArgumentException("Invalid order status: " + orderStatus);

    }
    return orderRepository.findByOrderStatusIgnoreCase(orderStatus);
    //TODO: Implement pagination and filtering
  }

  @Override
  public Order updateOrderStatus(Long orderId, String orderStatus) {
    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (orderStatus == null || orderStatus.isBlank()) {
      throw new IllegalArgumentException("Order status is required");
    }

    //Validate order status against allowed statuses
    if(!foundOrder.isOrderStatusValid(orderStatus.trim().toUpperCase())){
      throw new IllegalArgumentException("Invalid order status: " + orderStatus);
    }
    foundOrder.setOrderStatus(orderStatus.trim().toUpperCase());
    return orderRepository.save(foundOrder);
  }

  @Override
  @CachePut(value = "order", key = "#orderId")
  public Mono<Order> updateOrder(Long orderId, Order updateOrder) {

    //1. Find the existing order
    return Mono.justOrEmpty(orderRepository.findById(orderId)) //convert Optional to Mono
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
            .flatMap(existingOrder -> {
              //update basic details
              existingOrder.setCustomerName(updateOrder.getCustomerName());
              existingOrder.setCustomerEmail(updateOrder.getCustomerEmail());
              existingOrder.setShippingAddress(updateOrder.getShippingAddress());

              //validate and update order items
              return Flux.fromIterable(updateOrder.getOrderItems())
                      .flatMap(orderItem -> {
                        var productId = orderItem.getProductId();
                        var quantity = orderItem.getProductQuantity();
                        //a. call product service to get product details
                        return webClientBuilder.build()
                                .get()
                                .uri("http://product-service" + productServiceConfig.getBaseUrl() + "/product/" + productId)
                                .retrieve()
                                .bodyToMono(Result.class)
                                .map(result -> {
                                  if(result == null || result.getData() == null){
                                    throw new ProductNotFoundException(productId);
                                  }
                                  // Convert LinkedHashMap to ProductDto manually
                                  var objectMapper = new ObjectMapper(); //Jackson ObjectMapper
                                  objectMapper.registerModule(new JavaTimeModule());
                                  return objectMapper.convertValue(result.getData(), ProductDto.class);
                                })
                                .flatMap(productDto -> {
                                  if(productDto == null){
                                    return Mono.error(new ProductNotFoundException(productId));
                                  }
                                  //b. check if product quantity is available
                                  if (productDto.productQuantity() < quantity) {
                                    return Mono.error(new InsufficientStockQuantityException(productId));
                                  }
                                  //c update order item with validated product details
                                  orderItem.setProductName(productDto.productName());
                                  orderItem.setProductPrice(productDto.productPrice());
                                  //d. calculate the subtotal for the order item
                                  var itemTotal = productDto.productPrice().multiply(BigDecimal.valueOf(quantity));
                                  System.out.println("Product Price: " + itemTotal);
                                  System.out.println("Product Quantity: " + quantity);
                                  return Mono.just(orderItem);
                                });
                      })
                      .collectList()
                      .doOnNext(validatedOrderItems -> {

                        if (validatedOrderItems.isEmpty()) {
                          throw new IllegalArgumentException("No valid order items to update");
                        }
                        //update order with validated order items
                        existingOrder.getOrderItems().clear();
                        existingOrder.addOrderItems(validatedOrderItems);

                        //existingOrder.setOrderItems(validatedOrderItems);
                        //calculate total amount
                        var totalAmount = validatedOrderItems.stream()
                                .map(item -> item
                                        .getProductPrice()
                                        .multiply(BigDecimal.valueOf(item.getProductQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        existingOrder.setTotalAmount(totalAmount);
                        System.out.println("Final Total Amount: " + existingOrder.getTotalAmount());

                        //3. Deduct from Inventory
                        //ToDo: Implement Inventory deduction

                        //4. Set order status to "PLACED"
                        existingOrder.setOrderStatus("PLACED");

                      })
                      .then(Mono.just(existingOrder));

            }).flatMap(existingOrder -> Mono.fromCallable(() -> orderRepository.save(existingOrder))
                    .subscribeOn(Schedulers.boundedElastic())); //Wrap blocking call in a reactive context

  }

  /**
   * Use Transactional context to ensure that the operation is atomic
   * This prevents partial deletion of the order
   * @param orderId
   */
  @Transactional
  @Override
  public void deleteOrder(Long orderId) {

    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    //Delete the order(OrderItems will be deleted automatically due to CascadeType.ALL in Order class)
    orderRepository.delete(foundOrder);
  }

  @Override
  public void cancelOrder(Long orderId) {
    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    //soft delete the order
    foundOrder.setOrderStatus("CANCELLED");
    orderRepository.save(foundOrder);

  }

  @Override
  public void clearAllCache() {

  }
}
