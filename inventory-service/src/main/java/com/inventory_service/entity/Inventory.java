package com.inventory_service.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

/**NOTE:
 * Inventory entity class representing the inventory table in the database.
 * Since this is a microservices project, the relationship between the
 * productId here and that in the product service
 * will not be established by @OneToOne or @ManyToOne annotations.
 * When a product is created in the product service, the inventory service
 * subscribes to for example, ProductCreatedEvent and creates the inventory
 * via (messaging) event OR is called directly via REST API to create the initial
 * stock for the productId.
 * When order is created, order-service calls the inventory-service with the
 * productId to check/update the available stock.
 * IF WE ARE IN MONOLITHIC ARCHITECTURE, WE CAN USE @OneToOne or @ManyToOne
 * like this:
 * **********************
 * @OneToOne
 * @JoinColumn(name = "product_id", referencedColumnName = "product_id")
 * private Product product;
 * *************
 * IN SUMMARY:
 * 1.When product is added, product-service triggers POST /api/v1/inventory
 * Or USE KAFKA: product-service emits a ProductCreatedEvent
 * and inventory-service listens and create initial stock.
 * 2. There is no JPA relationship annotation like @ManyToOne or @OneToOne between them.
 */
@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long inventoryId;
  @Column(nullable = false, unique = true)
  private UUID productId; // Reference to the product ID from the Product Service
  @Column(nullable = false)
  private Integer availableStock;
  @Column(nullable = false)
  private Integer reservedQuantity;

  public Inventory() {
  }
  public Inventory(Long inventoryId, UUID productId, Integer availableStock, Integer reservedQuantity) {
    this.inventoryId = inventoryId;
    this.productId = productId;
    this.availableStock = availableStock;
    this.reservedQuantity = reservedQuantity;
  }
  public Long getInventoryId() {
    return inventoryId;
  }
  public void setInventoryId(Long inventoryId) {
    this.inventoryId = inventoryId;
  }
  public UUID getProductId() {
    return productId;
  }
  public void setProductId(UUID productId) {
    this.productId = productId;
  }
  public Integer getAvailableStock() {
    return availableStock;
  }
  public void setAvailableStock(Integer availableStock) {
    this.availableStock = availableStock;
  }
  public Integer getReservedQuantity() {
    return reservedQuantity;
  }
  public void setReservedQuantity(Integer reservedQuantity) {
    this.reservedQuantity = reservedQuantity;
  }
  @Override
  public String toString() {
    return "Inventory{" +
            "inventoryId=" + inventoryId +
            ", productId=" + productId +
            ", availableStock=" + availableStock +
            ", reservedQuantity=" + reservedQuantity +
            '}';
  }

}
