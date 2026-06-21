/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.exception;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.UsernameAlreadyExistsException;
import com.tjtechy.modelNotFoundException.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlingAdvice {
  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleIllegalArgumentException(IllegalArgumentException ex) {
    return new Result(ex.getMessage(), false, StatusCode.BAD_REQUEST);
  }

  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleUserNotFoundException(Exception ex) {
    return new Result(ex.getMessage(), false, StatusCode.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result handleOtherExceptions(Exception ex) {
    return new Result("A server internal error occurs", false, ex.getMessage(), StatusCode.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result handleAuthenticationExceptions(Exception ex) {
    return new Result("username or password is incorrect", false, ex.getMessage(), StatusCode.UNAUTHORIZED);
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleUsernameAlreadyExistsException(Exception ex) {
    return new Result(ex.getMessage(), false, StatusCode.BAD_REQUEST);
  }
}
