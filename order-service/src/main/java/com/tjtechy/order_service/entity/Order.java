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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    /**
     * Value must not be {@code null} or empty.
     * The user phone number validation pattern:
     * ^: start String
     * (\\+\\d{1,3}[- ]?)?: Optional country code part
     * \\+\\d{1,3}: '+' followed by 1 to 3 digits (country code e.g +1, +44, +234)
     * [- ]?: Optional separator (either a hyphen or space)
     * \\d{7,15}: Main phone number part (7 to 15 digits)
     * $: end String
     * All these will be invalid:
     * 12345 (too short)
     * 1234567890123456 (too long)
     * +1-234-567-8901-2345 (because of multiple separators)
     */
    @Column
    @Size(min = 7, max = 15, message = "Customer phone must be between 1 and 15 characters")
    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
            message = "Invalid phone number format"
    )
    private String customerPhone; // Optional field for phone number

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

    public Order(Long orderId, String customerName, String customerEmail, String customerPhone, String shippingAddress, BigDecimal totalAmount, LocalDate orderDate, String orderStatus, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
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

    public String getCustomerPhone() {
        return customerPhone;
    }
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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
                ", customerPhone='" + customerPhone + '\'' +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                ", orderStatus='" + orderStatus + '\'' +
                ", orderItems=" + orderItems +
                '}';
    }
}
