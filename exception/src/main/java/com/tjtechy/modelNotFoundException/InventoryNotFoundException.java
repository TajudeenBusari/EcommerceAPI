/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the exception module of the Ecommerce Microservices project.
 */
package com.tjtechy.modelNotFoundException;

import java.util.List;

public class InventoryNotFoundException extends RuntimeException{
  public InventoryNotFoundException(Long id) {
    super("Inventory not found with id: " + id);
  }
  public InventoryNotFoundException(List<Long> ids) {
    super("Inventory not found with ids: " + ids); }
}
