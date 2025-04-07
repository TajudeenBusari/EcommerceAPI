package com.inventory_service.entity.dto;

import java.util.UUID;

/**
 * InventoryDto: Returned by the inventory service
 * @param inventoryId
 * @param productId
 * @param reservedQuantity
 */
public record InventoryDto(
        Long inventoryId,
        UUID productId,
        Integer reservedQuantity

) {

}
