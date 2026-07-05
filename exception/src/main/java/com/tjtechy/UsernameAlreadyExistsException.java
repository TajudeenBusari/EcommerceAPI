/*
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the exception module of the Ecommerce Microservices project.
 */
package com.tjtechy;

public class UsernameAlreadyExistsException extends RuntimeException{
  public UsernameAlreadyExistsException(String username) {
    super("User already exists with username: " + username);
  }
}
