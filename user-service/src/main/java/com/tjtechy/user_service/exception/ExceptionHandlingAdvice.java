/*
 * Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of the user-service module of the Ecommerce Microservices project.
 */

package com.tjtechy.user_service.exception;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.UsernameAlreadyExistsException;
import com.tjtechy.modelNotFoundException.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    return new Result("A server internal error occurs: " + ex.getMessage(), false, StatusCode.INTERNAL_SERVER_ERROR);
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

  /**Used by WebFlux as against MethodArgumentNotValidException used in WebMvc
   * Handles exceptions of type {@link WebExchangeBindException}.
   * This method catches {@link WebExchangeBindException} which occurs when
   * request body parameters in controller fail validation annotations
   * (e.g., {@code @NotNull, @Size, @Min}).
   * The method extracts validation error details and returns them in a structured format.
   * Each field that caused the error is mapped to its corresponding validation message.
   * This is different from {@link IllegalArgumentException}
   * which is thrown when a method receives an invalid argument.
   * @param e The exception that contains validation errors.
   * @return {@link Result} object containing error messages mapped to invalid fields.
   * @see WebExchangeBindException
   * @see FieldError
   */
  @ExceptionHandler(WebExchangeBindException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleInvalidDataException(WebExchangeBindException e) {
    List<ObjectError> fieldErrors = e.getAllErrors();
    Map<String, String> map = new HashMap<>(fieldErrors.size());
    fieldErrors.forEach(error -> {
      String key = ((FieldError) error).getField();
      String value = error.getDefaultMessage();
      map.put(key, value);
    });

    return new Result("Provided arguments are invalid, see data for details", false, map, StatusCode.BAD_REQUEST);
  }

//  /**
//   * Handles exceptions of type {@link MethodArgumentNotValidException}.
//   * This method catches {@link MethodArgumentNotValidException} which occurs when
//   * request body parameters in controller fail validation annotations
//   * (e.g., {@code @NotNull, @Size, @Min}).
//   * The method extracts validation error details and returns them in a structured format.
//   * Each field that caused the error is mapped to its corresponding validation message.
//   * This is different from {@link IllegalArgumentException}
//   * which is thrown when a method receives an invalid argument.
//   * @param e The exception that contains validation errors.
//   * @return {@link Result} object containing error messages mapped to invalid fields.
//   * @see MethodArgumentNotValidException
//   * @see FieldError
//   */
//  @ExceptionHandler(MethodArgumentNotValidException.class)
//  @ResponseStatus(HttpStatus.BAD_REQUEST)
//  public Result handleInvalidDataException(MethodArgumentNotValidException e) {
//    List<ObjectError> fieldErrors = e.getAllErrors();
//    Map<String, String> map = new HashMap<>(fieldErrors.size());
//    fieldErrors.forEach(error -> {
//      String key = ((FieldError) error).getField();
//      String value = error.getDefaultMessage();
//      map.put(key, value);
//    });
//
//    return new Result("Provided arguments are invalid, see data for details", false, map, StatusCode.BAD_REQUEST);
//  }
}
