package com.tjtechy.order_service.exception;

import businessException.InsufficientStockQuantityException;
import com.tjtechy.system.Result;
import com.tjtechy.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlingAdvice {

  /**A general runtime exception that occurs when a method receives an invalid argument
   * that it cannot handle. It is part of Java core exception and extends {@link RuntimeException}.
   * When a method is called with an argument that is not valid or does not meet
   * the expected criteria. E.g., if order == null or order.getOrderItems().isEmpty()
   * or order.getOrderItems() == null.
   * Handles exceptions of type {@link IllegalArgumentException}.
   * @param e
   * @return
   */

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleIllegalArgumentException(IllegalArgumentException e) {
    return new Result(e.getMessage(), false, StatusCode.BAD_REQUEST);
  }


  /**
   * Handles exceptions of type {@link MethodArgumentNotValidException}.
   * This method catches {@link MethodArgumentNotValidException} which occurs when
   * request body parameters in controller fail validation annotations
   * (e.g., {@code @NotNull, @Size, @Min}).
   * The method extracts validation error details and returns them in a structured format.
   * Each field that caused the error is mapped to its corresponding validation message.
   * This is different from {@link IllegalArgumentException}
   * which is thrown when a method receives an invalid argument.
   * @param e The exception that contains validation errors.
   * @return {@link Result} object containing error messages mapped to invalid fields.
   * @see MethodArgumentNotValidException
   * @see FieldError
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleInvalidDataException(MethodArgumentNotValidException e) {
   List<ObjectError> fieldErrors = e.getAllErrors();
    Map<String, String> map = new HashMap<>(fieldErrors.size());
    fieldErrors.forEach(error -> {
      String key = ((FieldError) error).getField();
      String value = error.getDefaultMessage();
      map.put(key, value);
    });

    return new Result("Provided arguments are invalid, see data for details", false, map, StatusCode.BAD_REQUEST);
  }

  /**
   * Handles exceptions of type {@link InsufficientStockQuantityException}.
   * This method is triggered whenever an {@code InsufficientStockQuantityException} is thrown within the application.
   * It returns a standardized error response with an HTTP 400 (Bad Request) status.
   * @param e The exception instance containing details about the insufficient stock quantity.
   * @return A {@link Result} object containing an error message, a failure flag, and a corresponding status code.
   */
  @ExceptionHandler(InsufficientStockQuantityException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleInsufficientStockQuantityException(InsufficientStockQuantityException e) {
    return new Result(e.getMessage(), false, StatusCode.BAD_REQUEST);
  }

}
