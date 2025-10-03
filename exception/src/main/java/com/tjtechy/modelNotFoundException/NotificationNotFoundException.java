/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.modelNotFoundException;

import java.util.List;

public class NotificationNotFoundException extends RuntimeException{
  public NotificationNotFoundException(Long id) {
    super("Notification not found with id: " + id);
  }
  public NotificationNotFoundException(List<Long> ids) {
    super("Notification not found with ids: " + ids);
  }
}
