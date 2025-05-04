package com.tjtechy.inventory_service.mapper;

import com.tjtechy.CreateInventoryDto;
import com.tjtechy.InventoryDto;
import com.tjtechy.Inventory;
import com.tjtechy.UpdateInventoryDto;

import java.util.List;

public class InventoryMapper {
  public static InventoryDto mapFromInventoryToInventoryDto(Inventory inventory) {
    var inventoryDto = new InventoryDto(
            inventory.getInventoryId(),
            inventory.getProductId(),
            inventory.getReservedQuantity()
    );
    return inventoryDto;
  }

  /**
   * This method is not used in the codebase, but it is here for future reference
   * @param createInventoryDto
   * @return
   */
  public static InventoryDto mapFromCreateInventoryDtoToInventoryDto(CreateInventoryDto createInventoryDto) {
    var inventoryDto = new InventoryDto(
            null,
            createInventoryDto.productId(),
            createInventoryDto.reservedQuantity()
    );
    return inventoryDto;
  }

  /**
   * This method converts CreateInventoryDto to Inventory
   * @param createInventoryDto
   * @return Inventory
   */
  public static Inventory mapFromCreateInventoryDtoToInventory(CreateInventoryDto createInventoryDto) {
    var inventory = new Inventory(
            null,
            createInventoryDto.productId(),
            createInventoryDto.availableStock(),
            createInventoryDto.reservedQuantity()
    );
    return inventory;
  }

  /**
   * This method converts Inventory to InventoryDto
   * @param inventories
   * @return List<InventoryDto>
   */
  public static List<InventoryDto> mapFromInventoryListToInventoryDtoList(List<Inventory> inventories) {
    return inventories.stream()
            .map(InventoryMapper::mapFromInventoryToInventoryDto)
            .toList();
  }

  public static Inventory mapFromUpdateInventoryDtoToInventory(UpdateInventoryDto updateInventoryDto) {
    return new Inventory(
            null,
            updateInventoryDto.productId(),
            updateInventoryDto.availableStock(),
            updateInventoryDto.reservedQuantity()
    );
  }
}
