package com.tjtechy.modelNotFoundException;

import java.util.List;

public class InventoryNotFoundException extends RuntimeException{
  public InventoryNotFoundException(Long id) {
    super("Inventory not found with id: " + id);
  }
  public InventoryNotFoundException(List<Long> ids) {
    super("Inventory not found with ids: " + ids); }
}
