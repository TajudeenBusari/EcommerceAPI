/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.modelNotFoundException;

import java.util.List;
import java.util.UUID;

public class UserNotFoundException extends RuntimeException{

  public UserNotFoundException(UUID ID){
    super("User not found with id: " + ID);
  }

  public UserNotFoundException(List<UUID> IDS){
    super("Users not found with ids: " + IDS);
  }
}
