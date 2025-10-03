/**
 * Copyright © 2025
 *
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of EcommerceMicroservices module of the Ecommerce Microservices project.
 */

package com.tjtechy.notification_service.exception;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.modelNotFoundException.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlingAdvice {

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleIllegalArgumentException(IllegalArgumentException exception) {
    return new Result(exception.getMessage(), false, StatusCode.BAD_REQUEST);
  }

  @ExceptionHandler(NotificationNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleNotificationNotFoundException(NotificationNotFoundException exception) {
    return new Result(exception.getMessage(), false, StatusCode.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result handleGeneralException(Exception exception) {
    return new Result("A server internal error occurs" + exception.getMessage(), false, StatusCode.INTERNAL_SERVER_ERROR);
  }

}
