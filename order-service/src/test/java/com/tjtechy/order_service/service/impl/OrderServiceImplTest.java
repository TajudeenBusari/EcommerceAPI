package com.tjtechy.order_service.service.impl;

import com.tjtechy.*;
import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.tjtechy.client.InventoryServiceClient;
import com.tjtechy.client.ProductServiceClient;
import com.tjtechy.modelNotFoundException.OrderNotFoundException;
import com.tjtechy.order_service.config.InventoryServiceConfig;
import com.tjtechy.order_service.config.ProductServiceConfig;
import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.OrderItem;
import com.tjtechy.order_service.mapper.OrderMapper;
import com.tjtechy.order_service.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;

@ExtendWith(MockitoExtension.class) //Enable Mockito annotations
/**
 *  @MockitoSettings(strictness = Strictness.LENIENT)
 *  Allow lenient stubbing. This is useful when you want to avoid strict stubbing errors
 * especially when some mocks are not used in every test case.
 * This is particularly useful in large test classes where some mocks may not be relevant to every test.
 * This allows for more flexibility in test design, especially when dealing with complex interactions.
 */
  @MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private WebClient.Builder webClientBuilder;

  @Mock
  private ProductServiceConfig productServiceConfig;

  @Mock
  private InventoryServiceConfig inventoryServiceConfig;

  @Mock
  private ProductServiceClient productServiceClient;

  @Mock
  private InventoryServiceClient inventoryServiceClient;



  private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplTest.class);


  @InjectMocks
  private OrderServiceImpl orderService;

  private List<Order> orderList;



  @BeforeEach
  void setUp() {

//    MockitoAnnotations.openMocks(this);

    // Create a list of order items
    List<OrderItem> orderItem1 = Arrays.asList(
            new OrderItem(1L, UUID.randomUUID(), "PRODUCT1", new BigDecimal(100.00), 10),
            new OrderItem(2L, UUID.randomUUID(), "PRODUCT2", new BigDecimal(200.00), 20),
            new OrderItem(3L, UUID.randomUUID(), "PRODUCT3", new BigDecimal(300.00), 30)
    );
    List<OrderItem> orderItem2 = Arrays.asList(
            new OrderItem(4L, UUID.randomUUID(), "PRODUCT4", new BigDecimal(400.00), 40),
            new OrderItem(5L, UUID.randomUUID(), "PRODUCT5", new BigDecimal(500.00), 50),
            new OrderItem(6L, UUID.randomUUID(), "PRODUCT6", new BigDecimal(600.00), 60));

    List<OrderItem> orderItem3 = Arrays.asList(
            new OrderItem(7L, UUID.randomUUID(), "PRODUCT7", new BigDecimal(700.00), 70),
            new OrderItem(8L, UUID.randomUUID(), "PRODUCT8", new BigDecimal(800.00), 80));

    // Create a list of orders
    orderList = Arrays.asList(
            new Order(
                1L,
                "order 1 customer",
                "order1@email.com",
                "order 1 address",
                new BigDecimal(100.0),
                LocalDate.of(2025, 10, 10),
                "PLACED",
                orderItem1),

            new Order(
                2L,
                "order 2 customer",
                "order2@email.com",
                "order 2 address",
                new BigDecimal(200.0),
                LocalDate.of(2025, 10, 10),
                "SHIPPED",
                orderItem2),

            new Order(
                3L,
                "order 3 customer",
                "",
                "order 3 address",
                new BigDecimal(300.0),
                LocalDate.of(2025, 10, 10),
                "DELIVERED",
                orderItem3
            ),
            new Order(
                    4L,
                    "order 4 customer",
                    "",
                    "order 3 address",
                    new BigDecimal(400.0),
                    LocalDate.of(2025, 11, 10),
                    "CANCELLED",
                    orderItem3
            ),
            new Order(
                    5L,
                    "order 5 customer",
                    "order1@email.com",
                    "order 5 address",
                    new BigDecimal(500.0),
                    LocalDate.of(2025, 12, 10),
                    "PLACED",
                    orderItem3
            )

        );

  }

  @AfterEach
  void tearDown() {
  }



  @Test
  void testCreateOrderReactivelySuccess() {
    // Given
    UUID productId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID productId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    Order order = new Order();
    order.setOrderItems(Arrays.asList(
            new OrderItem(1L, productId1, null, null, 2),
            new OrderItem(2L, productId2, null, null, 3)
    ));

    ProductDto productDto1 = new ProductDto(
            productId1, "PRODUCT1", "CATEGORY", "DESCRIPTION", 10, 5,
            LocalDate.now().plusYears(1), new BigDecimal("10.00")
    );
    ProductDto productDto2 = new ProductDto(
            productId2, "PRODUCT2", "CATEGORY", "DESCRIPTION", 10, 5,
            LocalDate.now().plusYears(1), new BigDecimal("20.00")
    );

    Result productResult1 = new Result("Get One Success", true, productDto1, StatusCode.SUCCESS);
    Result productResult2 = new Result("Get One Success", true, productDto2, StatusCode.SUCCESS);

    Result inventoryResult1 = new Result("Inventory deducted successfully", true,
            new InventoryDto(1L, productId1, 2), StatusCode.SUCCESS);
    Result inventoryResult2 = new Result("Inventory deducted successfully", true,
            new InventoryDto(2L, productId2, 3), StatusCode.SUCCESS);


    when(productServiceConfig.getBaseUrl()).thenReturn("/api/v1");
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    when(webClientBuilder.build()).thenAnswer(invocation -> {
      WebClient webClient = mock(WebClient.class);

      // Mock GET product call chain
      WebClient.RequestHeadersUriSpec<?> getUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
      WebClient.RequestHeadersSpec<?> getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
      WebClient.ResponseSpec getResponseSpec = mock(WebClient.ResponseSpec.class);

      doReturn(getUriSpec).when(webClient).get();
      doReturn(getHeadersSpec).when(getUriSpec).uri(anyString());
      doReturn(getResponseSpec).when(getHeadersSpec).retrieve();

      when(getResponseSpec.bodyToMono(Result.class)).thenAnswer(invocationGet -> {
        String uri = (String) mockingDetails(getUriSpec).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("uri"))
                .reduce((first, second) -> second)
                .map(i -> (String) i.getArguments()[0])
                .orElse("");

        if (uri.endsWith(productId1.toString())) return Mono.just(productResult1);
        if (uri.endsWith(productId2.toString())) return Mono.just(productResult2);
        return Mono.error(new RuntimeException("Unknown product URI"));
      });

      // Mock PATCH inventory call chain
      WebClient.RequestBodyUriSpec patchUriSpec = mock(WebClient.RequestBodyUriSpec.class);
      WebClient.RequestBodySpec patchBodySpec = mock(WebClient.RequestBodySpec.class);
      WebClient.ResponseSpec patchResponseSpec = mock(WebClient.ResponseSpec.class);

      doReturn(patchUriSpec).when(webClient).patch();
      doReturn(patchBodySpec).when(patchUriSpec)
              .uri("http://inventory-service/api/v1/inventory/internal/deduct-inventory-reactive");
      doReturn(patchBodySpec).when(patchBodySpec).accept(MediaType.APPLICATION_JSON);
      doReturn(patchBodySpec).when(patchBodySpec).contentType(MediaType.APPLICATION_JSON);
      doReturn(patchBodySpec).when(patchBodySpec).bodyValue(any(DeductInventoryRequestDto.class));
      doReturn(patchResponseSpec).when(patchBodySpec).retrieve();

      when(patchResponseSpec.bodyToMono(Result.class)).thenReturn(
              Mono.just(inventoryResult1), Mono.just(inventoryResult2));

      return webClient;
    });

    given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

    // When
    Mono<Order> result = orderService.processOrderReactively(order);

    // Then
    StepVerifier.create(result)
            .assertNext(savedOrder -> {
              assertNotNull(savedOrder);
              assertEquals(2, savedOrder.getOrderItems().size());
              assertEquals("PRODUCT1", savedOrder.getOrderItems().get(0).getProductName());
              assertEquals("PRODUCT2", savedOrder.getOrderItems().get(1).getProductName());
              assertEquals(new BigDecimal("80.00"), savedOrder.getTotalAmount());
              assertEquals("PLACED", savedOrder.getOrderStatus());
            })
            .verifyComplete();
  }

  @Test
  void testCreateOrderReactivelyByCallingExternalServicesSuccess() {
    // Given
    UUID productId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Order order = new Order();
    order.setOrderItems(List.of(
            new OrderItem(1L, productId, null, null, 2)
    ));

    ProductDto productDto = new ProductDto(
            productId, "PRODUCT1", "CATEGORY", "DESCRIPTION", 10, 5,
            LocalDate.now().plusYears(1), new BigDecimal("10.00")
    );

    //not really needed here, but just to show how it can be used
    Result getProductResult = new Result("Get One Success", true, productDto, StatusCode.SUCCESS);

    //not really needed here, but just to show how it can be used
    Result inventoryDeductResult = new Result("Inventory deducted successfully", true,
            new InventoryDto(1L, productId, 1), StatusCode.SUCCESS);

    when(productServiceClient.getProductById(productId)).thenReturn(Mono.just(productDto));
    when(inventoryServiceClient.deductInventory(productId, 2)).thenReturn(Mono.empty());


    when(productServiceConfig.getBaseUrl()).thenReturn("/api/v1");
    when(inventoryServiceConfig.getBaseUrl()).thenReturn("/api/v1");

    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Mono<Order> result = orderService.processOrderReactivelyByCallingExternalizedServices(order);

    // Then
    StepVerifier.create(result)
            .assertNext(savedOrder -> {
              assertNotNull(savedOrder);
              assertEquals(1, savedOrder.getOrderItems().size());
              assertEquals("PRODUCT1", savedOrder.getOrderItems().get(0).getProductName());
              assertEquals(new BigDecimal("20.00"), savedOrder.getTotalAmount());
              assertEquals("PLACED", savedOrder.getOrderStatus());
            })
            .verifyComplete();
  }

  @Test
  void testCreateOrderReactivelyByCallingExternalizedServicesWithInsufficientQuantity() {
    // Given
    /**
     * TODO: I may need to refactor the check for insufficient stock quantity logic
     * in the OrderServiceImpl class by comparing the amount to be deducted (productQuantity in the orderItem)
     * with the availableStock in the product DB and not the productQuantity in the product DB.
     */
    UUID productId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Order order = new Order();
    order.setOrderItems(List.of(
            new OrderItem(1L, productId, null, null, 11) // Requesting more than available stock
    ));
    ProductDto productDto = new ProductDto(
            productId, "PRODUCT1", "CATEGORY", "DESCRIPTION", 10, 5,
            LocalDate.now().plusYears(1), new BigDecimal("10.00")
    );

    // Mock the product service to return the product with insufficient stock
    when(productServiceClient.getProductById(productId)).thenReturn(Mono.just(productDto));
    //inventory should not be called since the product has insufficient stock

    Mono<Order> result = orderService.processOrderReactivelyByCallingExternalizedServices(order);
    // When and Then
    StepVerifier.create(result)
            .expectError(InsufficientStockQuantityException.class)
            .verify();
  }


  @Test
  void getOrderByIdSuccess() {
    //Given
    var orderId = orderList.get(0).getOrderId();
    given(orderRepository.findById(orderId)).willReturn(Optional.of(orderList.get(0)));

    //When
    var order = orderService.getOrderById(orderId);

    //Then
    assertNotNull(order);
    assertEquals("order 1 customer", order.getCustomerName());
    assertEquals("order1@email.com", order.getCustomerEmail());
    assertEquals("order 1 address", order.getShippingAddress());
    assertEquals(new BigDecimal(100.0), order.getTotalAmount());
    assertEquals(LocalDate.of(2025, 10, 10), order.getOrderDate());
  }

  @Test
  void getOrderByIdOrderNotFound() {
    //Given
    var nonExistentOrderId = 900L;
    given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

    //when and //Then
    var exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(nonExistentOrderId));

    assertNotNull(exception);
    assertEquals(OrderNotFoundException.class, exception.getClass());

  }

  @Test
  void getAllOrdersSuccess() {
    //Given
    given(orderRepository.findAll()).willReturn(orderList);

    //convert order list to orderDto list
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(orderList);

    //When
    orderDtos = orderService.getAllOrders();

    //Then
    assertNotNull(orderDtos);
    assertEquals(5, orderDtos.size());
    assertEquals("order 1 customer", orderDtos.get(0).getCustomerName());
    assertEquals("order 2 customer", orderDtos.get(1).getCustomerName());
    assertEquals("order 3 customer", orderDtos.get(2).getCustomerName());
  }

  @Test
  void getAllOrdersWithoutCancelledOnesSuccess() {
    //Given
    //filter out cancelled orders
    List<Order> expectedOrders = orderList.stream()
                    .filter(order -> !order.getOrderStatus().equals("CANCELLED"))
                    .toList();
    given(orderRepository.findByOrderStatusNot("CANCELLED")).willReturn(expectedOrders);

    //convert order list to orderDto list
    var orderDtosWithoutCancelledOnes = OrderMapper.mapFromOrdersToOrderDtos(expectedOrders);

    //When
    orderDtosWithoutCancelledOnes = orderService.getAllOrdersWithoutCancelledOnes();

    //Then
    assertNotNull(orderDtosWithoutCancelledOnes);
    assertEquals(4, orderDtosWithoutCancelledOnes.size());
    assertEquals("order 1 customer", orderDtosWithoutCancelledOnes.get(0).getCustomerName());
    assertEquals("order 2 customer", orderDtosWithoutCancelledOnes.get(1).getCustomerName());
    assertEquals("order 3 customer", orderDtosWithoutCancelledOnes.get(2).getCustomerName());
  }

  @Test
  void getOrdersByCustomerEmailSuccess() {
    //Given
    List<Order> expectedOrders = orderList.stream()
            .filter(order -> order.getCustomerEmail().equals("order1@email.com"))
                    .toList();
    given(orderRepository.findByCustomerEmail("order1@email.com")).willReturn(expectedOrders);

    //convert order list to orderDto list
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(expectedOrders);

    //When
    orderDtos = orderService.getOrdersByCustomerEmail("order1@email.com");

    //Then
    assertNotNull(orderDtos);
    assertEquals(2, orderDtos.size());
    assertEquals("order 1 customer", orderDtos.get(0).getCustomerName());
    assertEquals("order 5 customer", orderDtos.get(1).getCustomerName());

  }

  @Test
  void getOrdersByStatusSuccess() {
    //Given
    //PLACED orders
    List<Order> expectedOrders = orderList.stream()
            .filter(order -> order.getOrderStatus().equals("PLACED"))
            .toList();
    given(orderRepository.findByOrderStatusIgnoreCase("PLACED")).willReturn(expectedOrders);

    //convert order list to orderDto list
    var orderDtos = OrderMapper.mapFromOrdersToOrderDtos(expectedOrders);

    //When
    orderDtos = orderService.getOrdersByStatus("PLACED");

    //Then
    assertNotNull(orderDtos);
    assertEquals(2, orderDtos.size());
    assertEquals("order 1 customer", orderDtos.get(0).getCustomerName());
    assertEquals("order 5 customer", orderDtos.get(1).getCustomerName());
  }


  @Test
  void updateOrderStatusSuccess() {
    //Given
    var orderId = orderList.get(0).getOrderId();
    var order = orderList.get(0);
    var newOrderStatus = "SHIPPED"; //Formally PLACED
    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    given(orderRepository.save(order)).willReturn(order);

    //When
    var updatedOrder = orderService.updateOrderStatus(orderId, newOrderStatus);

    //Then
    assertNotNull(updatedOrder);
    assertEquals("SHIPPED", updatedOrder.getOrderStatus());

  }

  @Test
  void updateOrderStatusOrderNotFound(){
    //Given
    var nonExistentOrderId = 900L;
    var newOrderStatus = "SHIPPED";
    given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

    //When and Then
    var exception = assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(nonExistentOrderId, newOrderStatus));

    assertNotNull(exception);
    assertEquals(OrderNotFoundException.class, exception.getClass());
  }

  @Test
  void updateOrderStatusOrderStatusIsNullOrEmpty(){
    //Given
    var orderId = orderList.get(0).getOrderId();
    var order = orderList.get(0);
    var newOrderStatus = ""; //covers the case where order status is null or empty
    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    //When and then
    var exception = assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(orderId, newOrderStatus));
    assertEquals(IllegalArgumentException.class, exception.getClass());
    assertEquals("Order status is required", exception.getMessage());
  }

  @Test
  void updateOrderStatusOrderStatusIsInvalid(){
    //Given
    var orderId = orderList.get(0).getOrderId();
    var order = orderList.get(0);
    var newOrderStatus = "INVALID"; //covers the case where order status is invalid
    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    //When and then
    var exception = assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(orderId, newOrderStatus));
    assertEquals(IllegalArgumentException.class, exception.getClass());
    assertEquals("Invalid order status: INVALID", exception.getMessage());
  }


  @Test
  void updateOrderSuccess() {

    //Given
    var orderId = 1L;
    var exist = new Order(
            orderId,
            "Original Customer",
            "original@email.com",
            "Original Address",
            BigDecimal.ZERO,
            LocalDate.now(),
            "PENDING",
            new ArrayList<>(Arrays.asList(
                    new OrderItem(1L, UUID.randomUUID(), "Old Product", BigDecimal.TEN, 1)
            ))
    );

   //create update order with the same ID
    // Create update order
    UUID productId = UUID.randomUUID();
    Order updateOrder = new Order();
    updateOrder.setCustomerName("Updated Customer");
    updateOrder.setCustomerEmail("updated@email.com");
    updateOrder.setShippingAddress("Updated Address");
    updateOrder.setOrderItems(new ArrayList<>(Arrays.asList(
            new OrderItem(null, productId, null, null, 2)  // New order item without ID
    )));

    System.out.println("Order ID: " + orderId);
    System.out.println("Existing Order ID: " + exist.getOrderId());

    // Ensure the IDs match
    assertEquals(orderId, exist.getOrderId(), "Order ID mismatch");

    //mock product response
    ProductDto productDto = new ProductDto(
            productId,
            "PRODUCT1",
            "PRODUCT1 CATEGORY",
            "PRODUCT1 DESCRIPTION",
            10,
            5,
            LocalDate.now().plusYears(1),
            new BigDecimal(10.0)
    );

    Result productDtoResult = new Result("Product retrieved successfully", true, productDto, StatusCode.SUCCESS);


    //mock repository behaviour
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(exist));
    given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
    doNothing().when(orderRepository).deleteOrderItemById(anyLong());


    //mock web client behaviour
    WebClient webClient = mock(WebClient.class);
    WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    given(webClientBuilder.build()).willReturn(webClient);
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    given(responseSpec.bodyToMono(Result.class)).willReturn(Mono.just(productDtoResult));

    Mono<Order> result = orderService.updateOrder(orderId, updateOrder);

    //Then
    StepVerifier.create(result)
            .assertNext(updatedOrder -> {
              assertNotNull(updatedOrder);
              assertEquals("Updated Customer", updatedOrder.getCustomerName());
              assertEquals("updated@email.com", updatedOrder.getCustomerEmail());
              assertEquals("Updated Address", updatedOrder.getShippingAddress());
              assertEquals("PLACED", updatedOrder.getOrderStatus());

              // Verify old order items are removed
              assertTrue(updatedOrder.getOrderItems()
                      .stream()
                      .noneMatch(orderItem -> orderItem.getProductName().equals("Old Product")));

              //VERIFY NEW ORDER ITEM IS ADDED
              // Verify new order item is added
              assertEquals(1, updatedOrder.getOrderItems().size());
              OrderItem newItem = updatedOrder.getOrderItems().get(0);
              assertEquals(productId, newItem.getProductId());
              assertEquals("PRODUCT1", newItem.getProductName());
              // Product price should be 10 (from mock)
              assertEquals(new BigDecimal("10"), newItem.getProductPrice());
              assertEquals(2, newItem.getProductQuantity());

              //verify the total amount is updated
              // Verify total amount is calculated correctly
              // Total should be price * quantity: 10 * 2 = 20
              var expectedTotal = productDto.productPrice().multiply(BigDecimal.valueOf(2));

              assertEquals(expectedTotal, updatedOrder.getTotalAmount());

            })
    .verifyComplete();


    verify(orderRepository).findById(orderId);
    verify(orderRepository).save(any(Order.class));

  }

  @Test
  void testUpdateOrderByCallingExternalizedServicesSuccess() {

    //given
    UUID productId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Order existingOrder = orderList.get(0);
    var orderId = existingOrder.getOrderId();
    /**
     * Don't use immutable list, that is, ##List.of(
     *     new OrderItem(1L, productId, "PRODUCT1", new BigDecimal("10.00"), 5)
     * )##
     * here because we need to clear the existing order items.
     * Calling clear() on an immutable list will throw UnsupportedOperationException.
     * However, use a mutable list like ArrayList (as used in the OrderServiceImpl logic) or LinkedList.
     */
   existingOrder.setOrderItems(new ArrayList<>(List.of(
           new OrderItem(1L, productId, "PRODUCT1", new BigDecimal("10.00"), 5)
   )));

    /**
     * customerName, customerEmail, shippingAddress and OrderItems can be updated
     */
   //update order with the same ID
    Order updateOrder = new Order();
    updateOrder.setOrderId(orderId);
    updateOrder.setCustomerName("Updated Customer"); //update customer name
    updateOrder.setCustomerEmail(existingOrder.getCustomerEmail()); //same customer email
    updateOrder.setShippingAddress(existingOrder.getShippingAddress()); //same shipping address
    //same list of order items
    /**
     * Don't use immutable list that is ##List.of(
     *     new OrderItem(1L, productId, "PRODUCT1", new BigDecimal("10.00"), 5)
     * )##
     * here because we need to clear the existing order items.
     * Calling clear() on an immutable list will throw UnsupportedOperationException.
     * However, use a mutable list like ArrayList (as used in the OrderServiceImpl logic) or LinkedList.
     */
    updateOrder.setOrderItems(new ArrayList<>(List.of(
            new OrderItem(1L, productId, "PRODUCT1", new BigDecimal("10.00"), 5)
    )));


    ProductDto productDto = new ProductDto(
            productId, "PRODUCT1", "CATEGORY", "DESCRIPTION", 10, 5,
            LocalDate.now().plusYears(1), new BigDecimal("10.00")
    );

    //mocks
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
    when(inventoryServiceClient.restoreInventory(productId, 5)).thenReturn(Mono.empty());

    doNothing().when(orderRepository).deleteOrderItemById(anyLong()); //orderItemId = 1L but we don't care about it here
    when(productServiceClient.getProductById(productId)).thenReturn(Mono.just(productDto));
    when(inventoryServiceClient.deductInventory(productId, 5)).thenReturn(Mono.empty());
    /**
     * This line of code means that the order will be saved with the updated order items.
     * invocation -> invocation.getArgument(0): gets the first argument passed to the save method,
     * that is returned as the updated order.
     */
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));


    //when
    Mono<Order> result = orderService.updateOrderByCallingExternalizedServices(orderId, updateOrder);

    //then
    StepVerifier.create(result)
            .assertNext(updatedOrder -> {
              assertNotNull(updatedOrder);
              assertEquals("Updated Customer", updatedOrder.getCustomerName());
              assertEquals(existingOrder.getCustomerEmail(), updatedOrder.getCustomerEmail());
              assertEquals(existingOrder.getShippingAddress(), updatedOrder.getShippingAddress());
              assertEquals("PLACED", updatedOrder.getOrderStatus());

              // Verify order items remain the same
              assertEquals(1, updatedOrder.getOrderItems().size());
              OrderItem orderItem = updatedOrder.getOrderItems().get(0);
              assertEquals(productId, orderItem.getProductId());
              assertEquals("PRODUCT1", orderItem.getProductName());
              assertEquals(new BigDecimal("10.00"), orderItem.getProductPrice());
              assertEquals(5, orderItem.getProductQuantity());

              // Verify total amount is calculated correctly
              BigDecimal expectedTotal = new BigDecimal("50.00"); // 10.00 * 5
              assertEquals(expectedTotal, updatedOrder.getTotalAmount());

            })
            .verifyComplete();

  }

  @Test
  void testDeleteOrderByCallingExternalizedServicesSuccess() {
    //Given
    UUID productId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Order existingOrder = orderList.get(0);
    var orderId = existingOrder.getOrderId();
    existingOrder.setOrderItems(new ArrayList<>(List.of(
            new OrderItem(1L, productId, "PRODUCT1", new BigDecimal("10.00"), 5)
    )));

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
    when(inventoryServiceClient.restoreInventory(productId, 5)).thenReturn(Mono.empty());
    doNothing().when(orderRepository).deleteOrderItemById(anyLong());

    //When
    orderService.deleteOrder(orderId);

    //Then
    verify(orderRepository, times(1)).findById(orderId);
    verify(orderRepository, times(1)).delete(existingOrder);
    verify(inventoryServiceClient, times(1)).restoreInventory(productId, 5);
  }

  @Test
  void testBulkDeleteOrdersSuccess() {
    //Given
    var productId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    var order1 = orderList.get(0);
    var orderId1 = orderList.get(0).getOrderId();
    order1.setOrderItems(new ArrayList<>(List.of(
            new OrderItem(1L, productId1, "PRODUCT1", new BigDecimal("10.00"), 3)
    )));
    var order2 = orderList.get(1);
    var orderId2 = orderList.get(1).getOrderId();
    order2.setOrderItems(new ArrayList<>(List.of(new OrderItem[]{
            new OrderItem(1L, productId1, "PRODUCT1", new BigDecimal("10.00"), 1)
    })));

    var existingOrdersIds = Arrays.asList(orderId1, orderId2);

    //mocks
    when(orderRepository.findAllById(existingOrdersIds)).thenReturn(orderList);
    /**
     * It better to use any(UUID.class) and anyInt() here to ensure that the method is
     * called with any UUID and any integer value. This will avoid issue with
     * inventoryServiceClient giving error when restoring inventory because we are
     * deleting multiple orders.
     */

    when(inventoryServiceClient.restoreInventory(any(UUID.class), anyInt())).thenReturn(Mono.empty());

    //When
    orderService.bulkDeleteOrders(existingOrdersIds);

    //Then
    verify(orderRepository, times(1)).findAllById(existingOrdersIds);
    verify(orderRepository, times(1)).deleteAll(orderList);
    verify(inventoryServiceClient, times(1)).restoreInventory(productId1, 3);
    verify(inventoryServiceClient, times(1)).restoreInventory(productId1, 1);
  }


  @Test
  void deleteOrderOrderNotFound() {
    //Given
    var nonExistentOrderId = 900L;
    given(orderRepository.findById(nonExistentOrderId)).willReturn(Optional.empty());

    //When and then
    var exception = assertThrows(RuntimeException.class, () -> orderService.deleteOrder(nonExistentOrderId));

    assertNotNull(exception);
    assertEquals(OrderNotFoundException.class, exception.getClass());
  }

  @Test
  void cancelOrderSuccess() {
    //Given
    var orderId = orderList.get(0).getOrderId();
    var order = orderList.get(0);
    var newOrderStatus = "CANCELLED"; //Formally PLACED
    order.setOrderStatus(newOrderStatus);

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    //mock restore inventory call
    given(inventoryServiceClient.restoreInventory(any(UUID.class), anyInt())).willReturn(Mono.empty());
    given(orderRepository.save(order)).willReturn(order);

    //When
    orderService.cancelOrder(orderId);

    //Then
    assertNotNull(order);
    assertEquals("CANCELLED", order.getOrderStatus());
  }

  @Test
  void clearAllCache() {
    //Given
    //When
    orderService.clearAllCache();

    //Then
    logger.info("*******All cache cleared successfully*******");
  }

  @Test
  void getOrderDtoById() {
    //Given
    var orderId = orderList.get(0).getOrderId();
    var order = orderList.get(0);

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    //map order to orderDto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(order);

    //When
    orderDto = orderService.getOrderDtoById(orderId);
    //Then
    assertNotNull(orderDto);
    assertEquals("order 1 customer", orderDto.getCustomerName());
  }
}