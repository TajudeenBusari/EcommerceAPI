/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.service.impl;


import com.netflix.discovery.converters.Auto;
import com.tjtechy.DeductInventoryRequestDto;
import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tjtechy.client.InventoryServiceClient;
import com.tjtechy.client.ProductServiceClient;
import com.tjtechy.modelNotFoundException.OrderNotFoundException;
import com.tjtechy.order_service.config.InventoryServiceConfig;
import com.tjtechy.order_service.config.ProductServiceConfig;
import com.tjtechy.order_service.entity.Order;

import com.tjtechy.order_service.entity.dto.OrderDto;
import com.tjtechy.order_service.mapper.OrderMapper;
import com.tjtechy.order_service.repository.OrderRepository;
import com.tjtechy.order_service.service.OrderService;
import com.tjtechy.Result;

import com.tjtechy.ProductDto;
import jakarta.transaction.Transactional;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  private final WebClient.Builder webClientBuilder;

  private final ProductServiceConfig productServiceConfig;

  private final InventoryServiceConfig inventoryServiceConfig;

  private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

  private final ProductServiceClient productServiceClient; //newly added for externalized service calls
  private final InventoryServiceClient inventoryServiceClient; //newly added for externalized service calls


  public OrderServiceImpl(OrderRepository orderRepository, WebClient.Builder webClientBuilder, ProductServiceConfig productServiceConfig, InventoryServiceConfig inventoryServiceConfig, ProductServiceClient productServiceClient, InventoryServiceClient inventoryServiceClient) {
    this.orderRepository = orderRepository;
    this.webClientBuilder = webClientBuilder;
    this.productServiceConfig = productServiceConfig;
    this.inventoryServiceConfig = inventoryServiceConfig;
    this.productServiceClient = productServiceClient;
    this.inventoryServiceClient = inventoryServiceClient;
  }

  /**NOTE:Currently this method is not being used in the application
   * Use Transactional context to ensure that the operation is atomic
   * For example, if the order creation fails, the transaction will be rolled back
   * A good example is if saving fails after deduction from Inventory, the
   * Inventory deduction should be reversed.
   * @param order
   * @return
   */
  @Deprecated //Will be removed in future versions
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
              .uri(productServiceConfig.getBaseUrl() + "/product/" + productId)//Using service name from Eureka
              //.uri(productServiceConfig.getBaseUrl() + "/" + productId)
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

      //TODO. Call inventory service to deduct stock

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
   * Use Transactional context to ensure that the operation is atomic
   * The method is implemented using reactive programming to create order,
   * avoid blocking and returns a Mono
   * @param order
   * @return
   */
  @Override
  @Transactional
  public Mono<Order> processOrderReactively(Order order) {

    return Flux.fromIterable(order.getOrderItems())
            .flatMap(orderItem -> {
              var productId = orderItem.getProductId();
              var quantity = orderItem.getProductQuantity();

              //a. call product service to get product details
              return webClientBuilder.build()
                      .get()
                      .uri("http://product-service" + productServiceConfig.getBaseUrl() + "/product/" + productId)
                      //.uri(productServiceConfig.getBaseUrl() + "/" + productId)
                      .retrieve()
                      .bodyToMono(Result.class)
                      .map(productResponse -> {
                        if (productResponse == null || productResponse.getData() == null) {
                          throw new ProductNotFoundException(productId);
                        }
                        // Convert LinkedHashMap to ProductDto manually
                        var objectMapper = new ObjectMapper(); //Jackson ObjectMapper
                        objectMapper.registerModule(new JavaTimeModule());
                        return objectMapper.convertValue(productResponse.getData(), ProductDto.class);
                      }).

                      flatMap(productDto -> {
                        if (productDto == null) {
                          return Mono.error(new ProductNotFoundException(productId));
                        }
                        //b. check if product quantity is available
                        if (productDto.productQuantity() < quantity) {
                          return Mono.error(new InsufficientStockQuantityException(productId));
                        }

//                       TODO. Call inventory service to deduct stock
                        //DONE: Call inventory service to deduct stock
                        var deductInventoryRequestDto = new DeductInventoryRequestDto(productId, quantity);
                        return webClientBuilder.build()
                                .patch()
                                .uri("http://inventory-service" + inventoryServiceConfig.getBaseUrl() + "/inventory/internal/deduct-inventory-reactive")
                                //.uri(inventoryServiceConfig.getBaseUrl() + "/internal/deduct-reactive")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(deductInventoryRequestDto)
                                .retrieve()
                                .bodyToMono(Result.class)
                                .flatMap(inventoryResponse -> {
                                  if (inventoryResponse == null || !inventoryResponse.isFlag()) {
                                    return Mono.error(new IllegalArgumentException("Failed to deduct inventory"));
                                  }

                                  //c update order item with validated product details
                                  orderItem.setProductName(productDto.productName());
                                  orderItem.setProductPrice(productDto.productPrice());
                                  orderItem.setOrder(order);
                                  //d. calculate the subtotal for the order item
                                  var itemTotal = productDto.productPrice().multiply(BigDecimal.valueOf(quantity));
                                  System.out.println("Product Price: " + itemTotal);
                                  System.out.println("Product Quantity: " + quantity);

                                  return Mono.just(itemTotal);
                                });

                      });

            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doOnNext(order::setTotalAmount)
            .then(Mono.defer(()-> {

              order.setOrderStatus("PLACED");
              return Mono.fromCallable(() -> orderRepository.save(order));
            }));

  }

  /**
   * Use Transactional context to ensure that the operation is atomic
   * The method is implemented using reactive programming to create order,
   * avoid blocking and returns a Mono
   * This method is used to create an order by calling externalized services like
   * Get Product and Deduct Inventory in the common-utils module.
   * The order service is the client of the product and inventory services.
   * @param order
   * @return
   */
  @Override
  @Transactional
  public Mono<Order> processOrderReactivelyByCallingExternalizedServices(Order order) {

    return Flux.fromIterable(order.getOrderItems())
            .flatMap(orderItem -> {
              var productId = orderItem.getProductId();
              var quantity = orderItem.getProductQuantity();

              //a. call product service to get product details
              return productServiceClient.getProductById(productId) //Using ProductServiceClient to call product service
                      .flatMap(productDto -> {
                        if (productDto == null) {
                          return Mono.error(new ProductNotFoundException(productId));
                        }
                        //b. check if product quantity is available
                        if (productDto.productQuantity() < quantity) {
                          return Mono.error(new InsufficientStockQuantityException(productId));
                        }
                        //Call inventory service to deduct stock
                        return inventoryServiceClient.deductInventory(productId, quantity) //Using InventoryServiceClient to call inventory service
                                .then(Mono.defer(() -> {
                                  //c update order item with validated product details
                                  orderItem.setProductName(productDto.productName());
                                  orderItem.setProductPrice(productDto.productPrice());
                                  orderItem.setOrder(order);
                                  //d. calculate the subtotal for the order item
                                  var itemTotal = productDto.productPrice().multiply(BigDecimal.valueOf(quantity));
                                  System.out.println("Product Price: " + itemTotal);
                                  System.out.println("Product Quantity: " + quantity);

                                  return Mono.just(itemTotal);
                                }));
                      });

            }).reduce(BigDecimal.ZERO, BigDecimal::add)
            .doOnNext(order::setTotalAmount)
            .then(Mono.defer(() -> {
              order.setOrderStatus("PLACED");
              return Mono.fromCallable(() -> orderRepository.save(order));
            }));
  }

  /**
   * Retrieve an order by its ID
   * @param orderId
   * @return
   * This is not being cached because the order entity it is
   * returning is giving issue during serialization in Redis.
   * The method will be removed from the class because
   * the OrderDto is being used to retrieve the order by ID
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
  @Cacheable(value = "orders")

  //DONE: The return type has been changed to OrderDto
  public List<OrderDto> getAllOrders() {

    var foundOrders = orderRepository.findAll();
    //Convert Orders to OrderDtos and return
    return  OrderMapper.mapFromOrdersToOrderDtos(foundOrders);
  }

  @Override
  @Cacheable(value = "ordersWithoutTheCancelledOnes")
  public List<OrderDto> getAllOrdersWithoutCancelledOnes() {

    var foundOrders = orderRepository.findByOrderStatusNot("CANCELLED");
    //Convert Orders to OrderDtos and return
    return  OrderMapper.mapFromOrdersToOrderDtos(foundOrders);
  }

  /**The method findOrderByEmail is a custom method created in the
   * OrderRepository interface
   * This method Retrieves all orders by customer email
   * @param customerEmail
   * @return
   */
  @Override
  @Cacheable(value = "orders", key = "#customerEmail")
  //TODO: Change the return type to OrderDto to fix the serialization issue
  //DONE: The return type has been changed to OrderDto
  public List<OrderDto> getOrdersByCustomerEmail(String customerEmail) {
    if (customerEmail == null || customerEmail.isEmpty()) {
      throw new IllegalArgumentException("Customer email is required");
    }
    var orders = orderRepository.findByCustomerEmail(customerEmail);
    //Convert Orders to OrderDtos and return
    return OrderMapper.mapFromOrdersToOrderDtos(orders);

    //TODO: Implement pagination and filtering
  }

  @Override
  @Cacheable(value = "orders", key = "#orderStatus")
  //TODO: Change the return type to OrderDto to fix the serialization issue
  //DONE: The return type has been changed to OrderDto
  public List<OrderDto> getOrdersByStatus(String orderStatus) {
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
    var foundOrders = orderRepository.findByOrderStatusIgnoreCase(orderStatus);

    //Convert Orders to OrderDtos and return
    return OrderMapper.mapFromOrdersToOrderDtos(foundOrders);

    //TODO: Implement pagination and filtering
  }

  @Override
  @CachePut(value = "order", key = "#orderId")
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

  /**
   * Update an existing order by its ID.
   * This method is missing logic to restore inventory for old items and deduct from inventory for new items.,
   * The complete logic has been implemented in the updateOrderByCallingExternalizedServices method.
   * This is left here for backward compatibility and will be removed in future versions.
   * @param orderId
   * @param updateOrder
   * @return
   */
  //TODO: update this code to restock inventory, deduct from inventory and update order items
  @Override
  @CachePut(value = "order", key = "#orderId")
  public Mono<Order> updateOrder(Long orderId, Order updateOrder) {

    //1. Find the existing order
    return Mono.justOrEmpty(orderRepository.findById(orderId)) //convert Optional to Mono
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
            .flatMap(existingOrder -> {
              System.out.println("Updating Order: " + existingOrder);

              //TODO: Restore inventory for old items here

              existingOrder.setCustomerName(updateOrder.getCustomerName());
              existingOrder.setCustomerEmail(updateOrder.getCustomerEmail());
              existingOrder.setShippingAddress(updateOrder.getShippingAddress());

              //delete old order items entities from the database
              var oldOrderItems = new ArrayList<>(existingOrder.getOrderItems());
              existingOrder.getOrderItems().clear();
              //delete old order items
              oldOrderItems.forEach(orderItem -> orderRepository.deleteOrderItemById(orderItem.getOrderItemId()));

              //validate and update order items
              return Flux.fromIterable(updateOrder.getOrderItems())
                      .flatMap(orderItem -> {
                        var productId = orderItem.getProductId();
                        var quantity = orderItem.getProductQuantity();

                        //a. call product service to get product details
                        return webClientBuilder.build()
                                .get()
                                .uri("http://product-service" + productServiceConfig.getBaseUrl() + "/product/" + productId)
                                //.uri(productServiceConfig.getBaseUrl() + "/" + productId)
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
                                  // Deduct from Inventory
                                  //ToDo: Implement Inventory deduction
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
                        existingOrder.addOrderItems(validatedOrderItems);

                        //calculate total amount
                        var totalAmount = validatedOrderItems.stream()
                                .map(item -> item
                                        .getProductPrice()
                                        .multiply(BigDecimal.valueOf(item.getProductQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        existingOrder.setTotalAmount(totalAmount);
                        System.out.println("Final Total Amount: " + existingOrder.getTotalAmount());


                        //4. Set order status to "PLACED"
                        existingOrder.setOrderStatus("PLACED");

                      })
                      .then(Mono.just(existingOrder));

            }).flatMap(existingOrder -> Mono.fromCallable(() -> orderRepository.save(existingOrder))
                    .subscribeOn(Schedulers.boundedElastic())); //Wrap blocking call in a reactive context

  }

  /**
   * Update an existing order by its ID.
   * This method is implemented using reactive programming to update order,
   * avoid blocking and returns a Mono
   * The method is implemented to call externalized services like
   * Get Product, Deduct Inventory and restock inventory in the common-utils module.
   * The order service is the client of the product and inventory services.
   * @param orderId
   * @param updateOrder
   * @return
   */
  @Override
  @CachePut(value = "order", key = "#orderId")
  @Transactional
  public Mono<Order> updateOrderByCallingExternalizedServices(Long orderId, Order updateOrder) {

    //1. Find the existing order
    return Mono.justOrEmpty(orderRepository.findById(orderId))
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
            .flatMap(existingOrder -> {
              System.out.println("Updating Order: " + existingOrder);

              //Restore inventory for old items
              return Flux.fromIterable(existingOrder.getOrderItems())
                      .flatMap(oldItem -> inventoryServiceClient.restoreInventory(
                              oldItem.getProductId(),
                              oldItem.getProductQuantity())
                      )
                      .then(Mono.defer(()-> {
                                //update logic here, update basic details
                                existingOrder.setCustomerName(updateOrder.getCustomerName());
                                existingOrder.setCustomerEmail(updateOrder.getCustomerEmail());
                                existingOrder.setShippingAddress(updateOrder.getShippingAddress());

                                //delete old order items entities from the database
                                var oldOrderItems = new ArrayList<>(existingOrder.getOrderItems());
                                existingOrder.getOrderItems().clear();
                                //delete old order items
                                oldOrderItems
                                        .forEach(orderItem ->
                                                orderRepository.deleteOrderItemById(orderItem.getOrderItemId()));

                                //validate and update order items
                                return Flux.fromIterable(updateOrder.getOrderItems())
                                        .flatMap(orderItem -> {
                                          var productId = orderItem.getProductId();
                                          var quantity = orderItem.getProductQuantity();

                                          //a. call product service to get product details
                                          return productServiceClient.getProductById(productId)
                                                  .flatMap(productDto -> {
                                                    if(productDto == null){
                                                      return Mono.error(new ProductNotFoundException(productId));
                                                    }
                                                    //b. check if product quantity is available
                                                    if (productDto.productQuantity() < quantity) {
                                                      return Mono.error(new InsufficientStockQuantityException(productId));
                                                    }

                                                    //DONE: Call inventory service to deduct stock
                                                    return inventoryServiceClient.deductInventory(productId, quantity)
                                                            .then(Mono.fromCallable(() -> {
                                                              // Enrich order item only after deduction
                                                              //c update order item with validated product details
                                                              orderItem.setProductName(productDto.productName());
                                                              orderItem.setProductPrice(productDto.productPrice());
                                                              orderItem.setOrder(existingOrder);
                                                              //d. calculate the subtotal for the order item
                                                              var itemTotal = productDto.productPrice().multiply(BigDecimal.valueOf(quantity));
                                                              System.out.println("Product Price: " + itemTotal);
                                                              System.out.println("Product Quantity: " + quantity);
                                                              return orderItem;

                                                            }));

                                                  });
                                        })
                                        .collectList()
                                        .doOnNext(validatedOrderItems -> {
                                          if (validatedOrderItems.isEmpty()) {
                                            throw new IllegalArgumentException("No valid order items to update");
                                          }
                                          //update order with validated order items
                                          existingOrder.addOrderItems(validatedOrderItems);
                                          //calculate total amount
                                          var totalAmount = validatedOrderItems.stream()
                                                  .map(item -> item
                                                          .getProductPrice()
                                                          .multiply(BigDecimal.valueOf(item.getProductQuantity())))
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                                          existingOrder.setTotalAmount(totalAmount);
                                          System.out.println("Final Total Amount: " + existingOrder.getTotalAmount());


                                          //4. Set order status to "PLACED"
                                          existingOrder.setOrderStatus("PLACED");
                                        })
                                        .then(Mono.just(existingOrder));
                              }));

            })
            .flatMap(existingOrder -> Mono.fromCallable(() -> orderRepository.save(existingOrder))
                    .subscribeOn(Schedulers.boundedElastic())); //Wrap blocking call in a reactive context
  }

  /**
   * Use Transactional context to ensure that the operation is atomic.
   * This prevents partial deletion of the order.
   * When order is deleted, it will restore inventory for all order items
   * @param orderId
   */
  @Transactional
  @Override
  @CacheEvict(value = "order", key = "#orderId")
  public void deleteOrder(Long orderId) {

    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

    //Restore inventory for order items before deleting the order
    foundOrder
            .getOrderItems()
            .forEach(orderItem -> {
      var productId = orderItem.getProductId();
      var quantity = orderItem.getProductQuantity();
      //Call inventory service to restore stock
              try{
                inventoryServiceClient.restoreInventory(productId, quantity)
                        .doOnError(ex ->
                                logger.error("Failed to restore inventory for product ID: {} with quantity: {}", productId, ex.getMessage()))
                        .subscribe();//fire and forget approach to keep the operation non-blocking
              }catch (Exception e){
                logger.error("Inventory restoration failed for product {}: {}", productId, e.getMessage());

              }
    });

    //Delete the order(OrderItems will be deleted automatically due to CascadeType.ALL in Order class)
    orderRepository.delete(foundOrder);
  }

  /**
   * Bulk delete orders by their IDs
   * This method will delete all orders with the given IDs
   * If any order ID is not found, it will throw OrderNotFoundException
   * @param orderIds
   */
  @Override
  public void bulkDeleteOrders(List<Long> orderIds) {
    var orders = orderRepository.findAllById(orderIds);

    //extract all found orders and collect them into a list
    var foundOrders = orders
            .stream()
            .map(Order::getOrderId)
            .toList();

    //get the order IDs that were not found
    var missingOrderIds = orderIds
            .stream()
            .filter(id -> !foundOrders.contains(id)).toList();

    //Restore inventory for all found orders before deleting the orders
    orders.forEach(order -> {
      order.getOrderItems().forEach(orderItem -> {
        var productId = orderItem.getProductId();
        var quantity = orderItem.getProductQuantity();
        //Call inventory service to restore stock
        try{
          inventoryServiceClient.restoreInventory(productId, quantity)
                  .doOnError(ex ->
                          logger.error("Could not restore inventory for product ID: {} with quantity: {}", productId, ex.getMessage()))
                  .subscribe();//fire and forget approach to keep the operation non-blocking
        }catch (Exception e){
          logger.error("Inventory failed to restore for product {}: {}", productId, e.getMessage());
        }
      });
    });
    //delete the found orders
    if (!orders.isEmpty()) {
      orderRepository.deleteAll(orders);
      logger.info("*******Deleted {} orders successfully*******", orders.size());
      } else if (!missingOrderIds.isEmpty()) {
      throw new OrderNotFoundException(missingOrderIds);
    }

  }

  /**
   * Cancel an order by its ID
   * This method will soft-delete the order by setting its status to "CANCELLED"
   * @param orderId
   */
  @Override
  public void cancelOrder(Long orderId) {
    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    //Restore inventory for order items before cancelling the order
    foundOrder
            .getOrderItems()
            .forEach(orderItem -> {
      var productId = orderItem.getProductId();
      var quantity = orderItem.getProductQuantity();
      //Call inventory service to restore stock
      try{
        inventoryServiceClient.restoreInventory(productId, quantity)
                .doOnError(ex ->
                        logger.error("Fail in restoring inventory for product ID: {} with quantity: {}", productId, ex.getMessage()))
                .subscribe();//fire and forget approach to keep the operation non-blocking
      }catch (Exception e){
        logger.error("Inventory restore failed for product {}: {}", productId, e.getMessage());
      }
    });
    //soft delete the order
    foundOrder.setOrderStatus("CANCELLED");
    orderRepository.save(foundOrder);

  }

  @Override
  @CacheEvict(value = "orders", allEntries = true)
  public void clearAllCache() {
    //clear all cache entries
    logger.info("*******Clearing all Order cache entries*******");
    logger.info("*******All Order cache cleared successfully*******");

  }

  @Override
  @Cacheable(value = "orderDto", key = "#orderId")
  @Transactional
  public OrderDto getOrderDtoById(Long orderId) {
    //1. Find the order
    var foundOrder = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    //2. Convert Order to OrderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(foundOrder);

    return orderDto;
  }


}
