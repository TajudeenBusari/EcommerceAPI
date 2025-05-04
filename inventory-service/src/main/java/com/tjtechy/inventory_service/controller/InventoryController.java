/**
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of inventory-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.inventory_service.controller;

import com.tjtechy.*;
import com.tjtechy.inventory_service.mapper.InventoryMapper;
import com.tjtechy.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.endpoint.base-url}/inventory")

public class InventoryController {

  private final InventoryService inventoryService;
  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  /**
   * This is method is used internally by the product service to create inventory
   * when a product is created. It is not exposed to the public.
   * But if you need to test in postman for example, you need to get the productId first from the product service
   * @param createInventoryDto
   * @return Result {@link Result}
   */
  @PostMapping("/internal/create")
  public Result addInventory(@Valid @RequestBody CreateInventoryDto createInventoryDto) {

    //map from createInventoryDto to inventory
    var inventory = InventoryMapper.mapFromCreateInventoryDtoToInventory(createInventoryDto);

    //call the service to add inventory
    var savedInventory = inventoryService.createInventory(inventory);

    //map from inventory to inventoryDto
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(savedInventory);

    return new Result("Inventory created successfully", true, inventoryDto, 200);
  }

  /**
   * This method is used to get inventory by id
   * @param inventoryId
   * @return Result {@link Result}
   */
  @GetMapping("/{inventoryId}")
  public Result getInventoryById(@PathVariable Long inventoryId) {

    //call the service to get inventory by id
    var inventory = inventoryService.getInventoryByInventoryId(inventoryId);

    //map from inventory to inventoryDto
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(inventory);

    return new Result("Inventory retrieved successfully", true, inventoryDto, StatusCode.SUCCESS);
  }

  @GetMapping
  public Result getAllInventory() {
    var inventories = inventoryService.getAllInventory();
    var inventoryDtos = InventoryMapper.mapFromInventoryListToInventoryDtoList(inventories);
    return new Result("All inventories retrieved successfully", true, inventoryDtos, StatusCode.SUCCESS);
  }

  /**
   * This method is used to get inventory by product id.
   * It is used internally by the product service.
   * @param productId
   * @return Result {@link Result}
   */
  @GetMapping("/internal/product/{productId}")
  public Result getInventoryByProductId(@PathVariable UUID productId) {

    //call the service to get inventory by product id
    var inventory = inventoryService.getInventoryByProductId(productId);

    //map from inventory to inventoryDto
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(inventory);

    return new Result("Inventory with productId: " + inventoryDto.productId() + " retrieved successfully", true, inventoryDto, StatusCode.SUCCESS);

  }

  /**
   * This method is used to update inventory, internally used by the product service
   * when a product is updated. The method is not exposed to the public.
   * @param inventoryId
   * @param updateInventoryDto
   * @return Result {@link Result}
   */
  @PutMapping("/internal/update/{inventoryId}")
  public Result updateInventory(@PathVariable Long inventoryId, @Valid @RequestBody UpdateInventoryDto updateInventoryDto) {

    //map from updateInventoryDto to inventory
    var inventory = InventoryMapper.mapFromUpdateInventoryDtoToInventory(updateInventoryDto);

    //call the service to update inventory
    var updatedInventory = inventoryService.updateInventory(inventoryId, inventory);

    //map from inventory to inventoryDto
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(updatedInventory);

    return new Result("Inventory updated successfully", true, inventoryDto, StatusCode.SUCCESS);

  }
  /**
   * This method is used to delete inventory
   * @param inventoryId
   * @return Result {@link Result}
   */
  @DeleteMapping("/{inventoryId}")
  public Result deleteInventory(@PathVariable Long inventoryId) {

    //call the service to delete inventory
    inventoryService.deleteInventory(inventoryId);

    return new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS);
  }

  @DeleteMapping("/bulk-delete")
  public Result bulkDeleteInventories(@RequestBody List<Long> inventoryIds) {

    //call the service to delete inventory
    inventoryService.bulkDeleteInventoriesByInventoryId(inventoryIds);

    return new Result("Inventories deleted successfully", true, null, StatusCode.SUCCESS);
  }



  /**
   * This method is used to deduct inventory by product id and quantity non-reactively and blocking.
   * @param deductInventoryRequestDto
   * @return Result {@link Result}
   */
  @PatchMapping("/internal/deduct-inventory")
  public Result deductInventory(@RequestBody DeductInventoryRequestDto deductInventoryRequestDto) {

    //call the service to deduct inventory
    inventoryService.deductInventory(deductInventoryRequestDto.productId(), deductInventoryRequestDto.quantity());
    return new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This method is used to deduct inventory reactively by product id and quantity
   * and non-blocking. It will be used in the order service to deduct inventory
   * @param deductInventoryRequestDto
   * @return Result {@link Result}
   */
  @PatchMapping("/internal/deduct-inventory-reactive")
  public Mono<Result> deductInventoryReactive(@RequestBody DeductInventoryRequestDto deductInventoryRequestDto) {

    //call the service to deduct inventory
    return inventoryService
            .deductInventoryReactive(deductInventoryRequestDto.productId(), deductInventoryRequestDto.quantity())
            .thenReturn(new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS));

  }



  /**
   * This method is used to delete inventory by product id.
   * @param productId
   * @return Result {@link Result}
   * Currently not used, but can be used in the future.
   */
//  @DeleteMapping("/internal/product/{productId}")
//  public Result deleteInventoryByProductId(@PathVariable UUID productId) {
//
//    //call the service to delete inventory
//    inventoryService.deleteInventoryByProductId(productId);
//
//    return new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS);
//  }


}

