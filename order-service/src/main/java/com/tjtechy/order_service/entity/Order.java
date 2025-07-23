/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of order-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.order_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The Order entity represents the order placed by a customer.
 * The order contains the customer's name, email, total amount, order date, order status, and order items.
 * The order items are the products that the customer has ordered.
 * The Order entity has a one-to-many relationship with the OrderItem entity.
 * The one side of the relationship is Order, and the many side of the relationship is OrderItem.
 */
@Entity
@Table(name = "orders")
public class Order implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    @Column(nullable = false)
    private String orderStatus; // PENDING, SHIPPED, DELIVERED, CANCELLED

    /**
     * //Relationship with product via OrderItem (composition)
     * The one side of the relationship is Order
     * The many side of the relationship is OrderItem
     * The JsonManagedReference annotation is used to manage
     * the relationship between Order and OrderItem to prevent infinite recursion.
     */
    //@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "order")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order", orphanRemoval = true)
    @JsonManagedReference //Manages the relationship between Order and OrderItem to prevent infinite recursion
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDate.now();
    }

    public Order(Long orderId, String customerName, String customerEmail, String shippingAddress, BigDecimal totalAmount, LocalDate orderDate, String orderStatus, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
    }



    public Order() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }


    /**
     * Add order items to the order
     * @param orderItems
     */
    public void addOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        if (orderItems != null){
            orderItems.forEach(orderItem -> orderItem.setOrder(this)); // Ensure each OrderItem has reference to Order
        }
    }

    /**
     * Helper method to validate order status
     * @return
     */
    public boolean isOrderStatusValid(String orderStatus) {
        if(orderStatus == null) return false;
        return List.of("PLACED", "SHIPPED", "DELIVERED", "CANCELLED").contains(orderStatus.trim().toUpperCase());
    }


    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }


}
