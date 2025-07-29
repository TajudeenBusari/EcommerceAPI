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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
  @Operation(summary = "Create Inventory",
          description = "This endpoint is used to create inventory for a product. It is used internally by the product service when a product is created.",
  responses = {
          @ApiResponse(responseCode = "200", description = "Inventory created successfully")
  })
  @PostMapping("/internal/create")
  public Result addInventory(@Valid @RequestBody CreateInventoryDto createInventoryDto) {

    var inventory = InventoryMapper.mapFromCreateInventoryDtoToInventory(createInventoryDto);
    var savedInventory = inventoryService.createInventory(inventory);
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(savedInventory);
    return new Result("Inventory created successfully", true, inventoryDto, 200);
  }

  /**
   * This method is used to get inventory by id
   * @param inventoryId
   * @return Result {@link Result}
   */
  @Operation(summary = "Get Inventory by ID",
          description = "This endpoint is used to get inventory by ID.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully")
          })
  @GetMapping("/{inventoryId}")
  public Result getInventoryById(@PathVariable Long inventoryId) {

    var inventory = inventoryService.getInventoryByInventoryId(inventoryId);
    var inventoryDto = InventoryMapper.mapFromInventoryToInventoryDto(inventory);
    return new Result("Inventory retrieved successfully", true, inventoryDto, StatusCode.SUCCESS);
  }

  @GetMapping
  @Operation(summary = "Get All Inventories",
          description = "This endpoint is used to get all inventories.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "All inventories retrieved successfully")
          })
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
  @Operation(summary = "Get Inventory by Product ID",
          description = "This endpoint is used to get inventory by product ID. It is used internally by the product service.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully")
          })
  @GetMapping("/internal/product/{productId}")
  public Result getInventoryByProductId(@PathVariable UUID productId) {

    var inventory = inventoryService.getInventoryByProductId(productId);
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
  @Operation(summary = "Update Inventory",
          description = "This endpoint is used to update inventory. It is used internally by the product service when a product is updated.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory updated successfully")
          })
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
  @Operation(summary = "Delete Inventory",
          description = "This endpoint is used to delete inventory by ID.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory deleted successfully")
          })
  @DeleteMapping("/{inventoryId}")
  public Result deleteInventory(@PathVariable Long inventoryId) {

    //call the service to delete inventory
    inventoryService.deleteInventory(inventoryId);

    return new Result("Inventory deleted successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This method is used to bulk delete inventories by inventory ids.
   * @param inventoryIds
   * @return Result {@link Result}
   */
  @Operation(summary = "Bulk Delete Inventories",
          description = "This endpoint is used to bulk delete inventories by inventory IDs.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventories deleted successfully")
          })
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
  @Operation(summary = "Deduct Inventory",
          description = "This endpoint is used to deduct inventory by product ID and quantity. It is used internally by the order service when an order is placed.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory deducted successfully")
          })
  @PatchMapping("/internal/deduct-inventory")
  public Result deductInventory(@RequestBody DeductInventoryRequestDto deductInventoryRequestDto) {

    inventoryService.deductInventory(deductInventoryRequestDto.productId(), deductInventoryRequestDto.quantity());
    return new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS);
  }

  /**
   * This method is used to deduct inventory reactively by product id and quantity
   * and non-blocking. It will be used in the order service to deduct inventory when
   * an order is placed/created.
   * @param deductInventoryRequestDto
   * @return Result {@link Result}
   */
  @Operation(summary = "Deduct Inventory Reactively",
          description = "This endpoint is used to deduct inventory reactively by product ID and quantity. It is used internally by the order service when an order is placed.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory deducted successfully")
          })
  @PatchMapping("/internal/deduct-inventory-reactive")
  public Mono<Result> deductInventoryReactive(@RequestBody DeductInventoryRequestDto deductInventoryRequestDto) {

    //call the service to deduct inventory
    return inventoryService
            .deductInventoryReactive(deductInventoryRequestDto.productId(), deductInventoryRequestDto.quantity())
            .thenReturn(new Result("Inventory deducted successfully", true, null, StatusCode.SUCCESS));
  }

  /**
   * This method is used to restore inventory by product id and quantity.
   * It is typically used internally by the Order-Service when an order is cancelled,
   * returned or updated and the inventory needs to be restored.
   * @param restoreInventoryDto
   * @return Result {@link Result}
   */
  @Operation(summary = "Restore Inventory",
          description = "This endpoint is used to restore inventory by product ID and quantity. It is typically used internally by the Order-Service " +
                  "when an order is deleted, cancelled, returned or updated and the inventory needs to be restored.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Inventory restored successfully")
          })
  @PostMapping("/internal/restore-inventory")
  public Result restoreInventory(@Valid @RequestBody RestoreInventoryDto restoreInventoryDto) {

    //call the service to restore inventory
    inventoryService.restoreInventoryStock(restoreInventoryDto.productId(), restoreInventoryDto.quantity());

    return new Result("Inventory restored successfully", true, null, StatusCode.SUCCESS);
  }


}

