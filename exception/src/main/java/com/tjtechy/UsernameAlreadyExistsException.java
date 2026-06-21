package com.tjtechy;

public class UsernameAlreadyExistsException extends RuntimeException{
  public UsernameAlreadyExistsException(String username) {
    super("User already exists with username: " + username);
  }
}
