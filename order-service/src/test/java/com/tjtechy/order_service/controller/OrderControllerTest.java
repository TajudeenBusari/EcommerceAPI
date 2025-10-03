package com.tjtechy.order_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.RedisCacheConfig;
import com.tjtechy.Result;
import com.tjtechy.order_service.entity.Order;
import com.tjtechy.order_service.entity.OrderItem;
import com.tjtechy.order_service.entity.dto.CreateOrderDto;
import com.tjtechy.order_service.entity.dto.OrderDto;
import com.tjtechy.order_service.entity.dto.OrderItemDto;
import com.tjtechy.order_service.entity.dto.UpdateOrderDto;
import com.tjtechy.order_service.mapper.OrderMapper;
import com.tjtechy.order_service.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.config.client.ConfigServerBootstrapper;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class) //Recommended for unit testing for controllers
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
                "api.endpoint.base-url=/api/v1",
                "spring.cache.type=none",
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.redis.enabled=false",
        }
)
@ImportAutoConfiguration(exclude = {
        // Exclude any autoconfiguration classes that are not needed for the test
        RedisCacheConfig.class,
        EurekaClientAutoConfiguration.class,
        ConfigServerBootstrapper.class
})
class OrderControllerTest {

  /**
   * The MockitoBean annotation is used to create a mock instance of the OrderService
   */
  @MockitoBean
  private OrderService orderService;

  @MockitoBean
  private RedisConnectionFactory redisConnectionFactory;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  private WebTestClient webTestClient; // Use WebTestClient for reactive testing

  @Autowired
  private MockMvc mockMvc;

  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  List<Order> orders;

  @BeforeEach
  void setUp() {

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

    orders = Arrays.asList(
            new Order(
                    1L,
                    "order 1 customer",
                    "order1@email.com",
                    "+1234567890",
                    "order 1 address",
                    new BigDecimal(100.0),
                    LocalDate.of(2025, 10, 10),
                    "PLACED",
                    orderItem1),
            new Order(
                    2L,
                    "order 2 customer",
                    "order2@email.com",
                    "+1987654321",
                    "order 2 address",
                    new BigDecimal(200.0),
                    LocalDate.of(2025, 10, 11),
                    "PLACED",
                    orderItem2)

    );
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void createOrderSuccess() throws Exception {
    //Given
    //create a list of order item dto
    List<OrderItemDto> orderItemDtos = Arrays.asList(
            new OrderItemDto(UUID.randomUUID(), "PRODUCT1", 10),
            new OrderItemDto(UUID.randomUUID(), "PRODUCT2", 20)

    );
    //create a createOrder dto
    CreateOrderDto createOrderDto = new CreateOrderDto();
    createOrderDto.setCustomerName("customer1 name");
    createOrderDto.setCustomerEmail("customer1@email.com");
    createOrderDto.setShippingAddress("customer1 address");
    createOrderDto.setOrderItems(orderItemDtos);

    var json = objectMapper.writeValueAsString(createOrderDto);

    //mock the save order of the order service
    var savedOrder = new Order();
    savedOrder.setOrderId(1L);
    savedOrder.setCustomerName("customer1 name");
    savedOrder.setOrderStatus("PLACED");
    savedOrder.setShippingAddress("customer1 address");
    savedOrder.setOrderDate(LocalDate.now());
    savedOrder.setTotalAmount(BigDecimal.TEN);
    savedOrder.setCustomerEmail("customer1@email.com");
    savedOrder.setOrderItems(new ArrayList<>());

    when(orderService.processOrderReactively(any(Order.class))).thenReturn(Mono.just(savedOrder));

    //map order to order dto
    var orderDto = OrderMapper.mapFromOrderToOrderDto(savedOrder);

    webTestClient.post()
            .uri(  baseUrl + "/order/reactive")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
                    .exchange()
            .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Order created successfully")
            .jsonPath("$.flag").isEqualTo(true)
            .jsonPath("$.data.customerName").isEqualTo(orderDto.getCustomerName())
            .jsonPath("$.data.customerEmail").isEqualTo(orderDto.getCustomerEmail())
            .jsonPath("$.data.shippingAddress").isEqualTo(orderDto.getShippingAddress());

    //Then: Verify Interactions
    verify(orderService).processOrderReactively(any(Order.class));

  }

  @Test
  void getOrderDtoByIdSuccess() throws Exception {
    //Given
    var orderDto = new OrderDto();
    orderDto.setOrderId(1L);
    orderDto.setCustomerName("customer1 name");
    orderDto.setOrderStatus("PLACED");
    orderDto.setShippingAddress("customer1 address");
    orderDto.setOrderDate(LocalDate.now());
    orderDto.setTotalAmount(BigDecimal.TEN);

    //when
    when(orderService.getOrderDtoById(1L)).thenReturn(orderDto);

    //then
    mockMvc.perform(get(baseUrl + "/order/orderDto/{orderId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("OrderDto retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.orderId").value(orderDto.getOrderId()))
            .andExpect(jsonPath("$.data.customerName").value(orderDto.getCustomerName()))
            .andExpect(jsonPath("$.data.customerEmail").value(orderDto.getCustomerEmail()))
            .andExpect(jsonPath("$.data.shippingAddress").value(orderDto.getShippingAddress()));
  }


  @Test
  void getAllOrderDtosSuccess() throws Exception {
    // Given
    List<OrderDto> orderDtos = new ArrayList<>();

    OrderDto orderDto1 = new OrderDto();
    orderDto1.setOrderId(1L);
    orderDto1.setCustomerName("customer1 name");
    orderDto1.setOrderStatus("PLACED");
    orderDto1.setShippingAddress("customer1 address");
    orderDto1.setOrderDate(LocalDate.now());
    orderDto1.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto1);

    OrderDto orderDto2 = new OrderDto();
    orderDto2.setOrderId(2L);
    orderDto2.setCustomerName("customer2 name");
    orderDto2.setOrderStatus("PLACED");
    orderDto2.setShippingAddress("customer2 address");
    orderDto2.setOrderDate(LocalDate.now());
    orderDto2.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto2);
    // Mock the service method
    when(orderService.getAllOrders()).thenReturn(orderDtos);
    // Perform the GET request
    mockMvc.perform(get(baseUrl + "/order")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].orderId").value(orderDto1.getOrderId()))
            .andExpect(jsonPath("$.data[0].customerName").value(orderDto1.getCustomerName()))
            .andExpect(jsonPath("$.data[0].customerEmail").value(orderDto1.getCustomerEmail()))
            .andExpect(jsonPath("$.data[0].shippingAddress").value(orderDto1.getShippingAddress()))
            .andExpect(jsonPath("$.data[1].orderId").value(orderDto2.getOrderId()))
            .andExpect(jsonPath("$.data[1].customerName").value(orderDto2.getCustomerName()))
            .andExpect(jsonPath("$.data[1].customerEmail").value(orderDto2.getCustomerEmail()))
            .andExpect(jsonPath("$.data[1].shippingAddress").value(orderDto2.getShippingAddress()));
  }

  @Test
  void getOrdersByCustomerEmail() throws Exception {
    // Given
    List<OrderDto> orderDtos = new ArrayList<>();

    OrderDto orderDto1 = new OrderDto();
    orderDto1.setOrderId(1L);
    orderDto1.setCustomerName("customer1 name");
    orderDto1.setOrderStatus("PLACED");
    orderDto1.setShippingAddress("customer1 address");
    orderDto1.setOrderDate(LocalDate.now());
    orderDto1.setCustomerEmail("customer1@email.com");
    orderDto1.setTotalAmount(BigDecimal.TEN);
    orderDto1.setOrderItems(new ArrayList<>());

    orderDtos.add(orderDto1);

    OrderDto orderDto2 = new OrderDto();
    orderDto2.setOrderId(2L);
    orderDto2.setCustomerName("customer2 name");
    orderDto2.setOrderStatus("PLACED");
    orderDto2.setShippingAddress("customer2 address");
    orderDto2.setOrderDate(LocalDate.now());
    orderDto2.setCustomerEmail("customer2@email.com");
    orderDto2.setTotalAmount(BigDecimal.TEN);
    orderDto2.setOrderItems(new ArrayList<>());

    orderDtos.add(orderDto2);

    OrderDto orderDto3 = new OrderDto();
    orderDto3.setOrderId(3L);
    orderDto3.setCustomerName("customer1 name");
    orderDto3.setOrderStatus("PLACED");
    orderDto3.setShippingAddress("customer1 address");
    orderDto3.setOrderDate(LocalDate.now());
    orderDto3.setCustomerEmail("customer1@email.com");
    orderDto3.setTotalAmount(BigDecimal.TEN);
    orderDto3.setOrderItems(new ArrayList<>());

    orderDtos.add(orderDto3);
     List<OrderDto> expectedOrderDtos = orderDtos
             .stream()
             .filter(orderDto ->
                     orderDto.getCustomerEmail()
                             .equals("customer1@email.com"))
              .toList();
    when(orderService.getOrdersByCustomerEmail("customer1@email.com")).thenReturn(expectedOrderDtos);
    // Perform the GET request
    mockMvc.perform(get(baseUrl + "/order/customer")
            .param("customerEmail", "customer1@email.com")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Orders by email retrieved successfully"))
    .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].orderId").value(orderDto1.getOrderId()))
            .andExpect(jsonPath("$.data[0].customerName").value(orderDto1.getCustomerName()))
            .andExpect(jsonPath("$.data[0].customerEmail").value(orderDto1.getCustomerEmail()))
            .andExpect(jsonPath("$.data[0].shippingAddress").value(orderDto1.getShippingAddress()))
            .andExpect(jsonPath("$.data[1].orderId").value(orderDto3.getOrderId()))
            .andExpect(jsonPath("$.data[1].customerName").value(orderDto3.getCustomerName()))
            .andExpect(jsonPath("$.data[1].customerEmail").value(orderDto3.getCustomerEmail()))
            .andExpect(jsonPath("$.data[1].shippingAddress").value(orderDto3.getShippingAddress()));

  }

  @Test
  void deleteOrderSuccess() throws Exception {
    // Given
    Long orderId = 1L;

    // Mock the service method
    doNothing().when(orderService).deleteOrder(orderId);

    // Perform the DELETE request
    mockMvc.perform(delete(baseUrl + "/order/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Order deleted successfully"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  @Test
  void testBulkDeleteOrdersSuccess() throws Exception {
    // Given
    List<Long> orderIds = Arrays.asList(1L, 2L);
    // Create a JSON array of order IDs
    String orderIdsJson = objectMapper.writeValueAsString(orderIds);


    // Mock the service method
    doNothing().when(orderService).bulkDeleteOrders(orderIds);

    // Perform the DELETE request
    mockMvc.perform(delete(baseUrl + "/order/bulk-delete")
                    .content(orderIdsJson)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Orders bulk deleted success"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  @Test
  void getAllOrdersWithoutCancelledOnesSuccess() throws Exception {
    // Given
    List<OrderDto> orderDtos = new ArrayList<>();

    OrderDto orderDto1 = new OrderDto();
    orderDto1.setOrderId(1L);
    orderDto1.setCustomerName("customer1 name");
    orderDto1.setOrderStatus("PLACED");
    orderDto1.setShippingAddress("customer1 address");
    orderDto1.setOrderDate(LocalDate.now());
    orderDto1.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto1);

    OrderDto orderDto2 = new OrderDto();
    orderDto2.setOrderId(2L);
    orderDto2.setCustomerName("customer2 name");
    orderDto2.setOrderStatus("CANCELLED");
    orderDto2.setShippingAddress("customer2 address");
    orderDto2.setOrderDate(LocalDate.now());
    orderDto2.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto2);

    // Mock the service method
    when(orderService.getAllOrdersWithoutCancelledOnes()).thenReturn(orderDtos.stream()
            .filter(order -> !order.getOrderStatus().equals("CANCELLED"))
            .toList());

    // Perform the GET request
    mockMvc.perform(get(baseUrl + "/order/without-cancelled")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].orderId").value(orderDto1.getOrderId()))
            .andExpect(jsonPath("$.data[0].customerName").value(orderDto1.getCustomerName()))
            .andExpect(jsonPath("$.data[0].customerEmail").value(orderDto1.getCustomerEmail()))
            .andExpect(jsonPath("$.data[0].orderStatus").value(orderDto1.getOrderStatus()))
            .andExpect(jsonPath("$.data[0].shippingAddress").value(orderDto1.getShippingAddress()));
  }

  @Test
  void cancelOrderSuccess() throws Exception {
    // Given
    var orderId = orders.get(0).getOrderId();

    // Mock the service method
    doNothing().when(orderService).cancelOrder(orderId);

    // Perform the DELETE request
    mockMvc.perform(delete(baseUrl + "/order/cancel/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
            .andExpect(jsonPath("$.flag").value(true));

  }

  @Test
  void getOrdersByStatusSuccess() throws Exception {
    // Given
    List<OrderDto> orderDtos = new ArrayList<>();

    OrderDto orderDto1 = new OrderDto();
    orderDto1.setOrderId(1L);
    orderDto1.setCustomerName("customer1 name");
    orderDto1.setOrderStatus("PLACED");
    orderDto1.setShippingAddress("customer1 address");
    orderDto1.setOrderDate(LocalDate.now());
    orderDto1.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto1);

    OrderDto orderDto2 = new OrderDto();
    orderDto2.setOrderId(2L);
    orderDto2.setCustomerName("customer2 name");
    orderDto2.setOrderStatus("CANCELLED");
    orderDto2.setShippingAddress("customer2 address");
    orderDto2.setOrderDate(LocalDate.now());
    orderDto2.setTotalAmount(BigDecimal.TEN);

    orderDtos.add(orderDto2);

    // Mock the service method
    when(orderService.getOrdersByStatus("CANCELLED")).thenReturn(orderDtos.stream()
            .filter(order -> order.getOrderStatus().equals("CANCELLED"))
            .toList());
    // Perform the GET request
    mockMvc.perform(get(baseUrl + "/order/status")
                    .param("orderStatus", "CANCELLED")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Orders by status retrieved successfully"))
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].orderId").value(orderDto2.getOrderId()))
            .andExpect(jsonPath("$.data[0].customerName").value(orderDto2.getCustomerName()))
            .andExpect(jsonPath("$.data[0].customerEmail").value(orderDto2.getCustomerEmail()))
            .andExpect(jsonPath("$.data[0].shippingAddress").value(orderDto2.getShippingAddress()));
  }

  @Test
  void updateOrderStatusSuccess() throws Exception {
    // Given
    var order = orders.get(0);
    var orderId = order.getOrderId();
    var updatedOrder = new Order();
    updatedOrder.setOrderId(orderId);
    updatedOrder.setOrderStatus("SHIPPED");

    // Mock the service method
   when(orderService.updateOrderStatus(orderId, updatedOrder.getOrderStatus())).thenReturn(updatedOrder);

    // Perform the PUT request
    mockMvc.perform(put(baseUrl + "/order/{orderId}/update-status", orderId)
                    .param("orderStatus", updatedOrder.getOrderStatus())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Order status updated successfully"))
            .andExpect(jsonPath("$.flag").value(true));
  }

  @Test
  void updateOrderSuccess() throws Exception {
    // Given
    //create an update request dto
    var updateOrderDto = new UpdateOrderDto(
            "updated customer name",
            "test@test.com",
            "+1234567890",
            "updated shipping address",
            new ArrayList<>(){
              {
                add(new OrderItemDto(UUID.randomUUID(), "PRODUCT1", 10));
                add(new OrderItemDto(UUID.randomUUID(), "PRODUCT2", 20));
              }
            }
    );

    var json = objectMapper.writeValueAsString(updateOrderDto);

    var order = orders.get(0);
    var orderId = order.getOrderId();


    // Mock the service method
    when(orderService.updateOrder(anyLong(), any(Order.class)))
            .thenReturn(Mono.just(order));

    // Perform the PUT request
    webTestClient.put()
            .uri(baseUrl + "/order/{orderId}", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Order updated successfully")
            .jsonPath("$.flag").isEqualTo(true);
  }

  @Test
  void testUpdateOrderByCallingExternalizedServices() throws Exception {
    //Given
    //create an update request dto
    var updateOrderDto = new UpdateOrderDto(
            "updated customer name",
            "test@test.com",
            "+1234567890",
            "updated shipping address",
            new ArrayList<>(){
              {
                add(new OrderItemDto(UUID.randomUUID(), "PRODUCT1", 10));
                add(new OrderItemDto(UUID.randomUUID(), "PRODUCT2", 20));
              }
            }
    );

    var json = objectMapper.writeValueAsString(updateOrderDto);

    var order = orders.get(0);
    var orderId = order.getOrderId();
    //mock service method
    when(orderService.updateOrderByCallingExternalizedServices(eq(orderId), any(Order.class))).thenReturn(Mono.just(order));

    //When and Then
    webTestClient.put()
            .uri(baseUrl + "/order/externalized/{orderId}", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.message").isEqualTo("Order updated successfully by calling required external services")
            .jsonPath("$.flag").isEqualTo(true);
  }

  @Test
  void clearCache() {
  }
}