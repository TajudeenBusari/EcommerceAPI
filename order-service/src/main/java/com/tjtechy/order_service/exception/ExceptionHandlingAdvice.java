package com.tjtechy.order_service.exception;


import com.tjtechy.businessException.InsufficientStockQuantityException;
import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.businessException.OrderAlreadyCancelledException;
import com.tjtechy.modelNotFoundException.OrderNotFoundException;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
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

  /**
   * Handles exceptions of type {@link OrderNotFoundException}.
   * <p>
   * This method is triggered whenever an {@code OrderNotFoundException} is thrown within the application.
   * It returns a standardized error response with an HTTP 404 (Not Found) status.
   * </p>
   * @param e The exception instance containing details about the missing order.
   * @return A {@link Result} object containing an error message, a failure flag, and a corresponding status code.
   */
  @ExceptionHandler(OrderNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleOrderNotFoundException(OrderNotFoundException e) {
    return new Result(e.getMessage(), false, StatusCode.NOT_FOUND);
  }

  /**
   * Handles exceptions of type {@link ProductNotFoundException}. It is important to handle
   * this exception here so that user will get a generic error message
   * <p>
   * This method is triggered whenever a {@code ProductNotFoundException} is thrown within the application.
   * It returns a standardized error response with an HTTP 404 (Not Found) status.
   * </p>
   * When user tries to create an order with a product that does not exist in the database,
   * this method will be triggered.
   * @param e The exception instance containing details about the missing product.
   * @return A {@link Result} object containing an error message, a failure flag, and a corresponding status code.
   */
  @ExceptionHandler(ProductNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleProductNotFoundException(ProductNotFoundException e) {
    return new Result(e.getMessage(), false, StatusCode.NOT_FOUND);
  }

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

  /**
   * Handle already cancelled order exception.
   */
  @ExceptionHandler(OrderAlreadyCancelledException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Result handleOrderAlreadyCancelledException(OrderAlreadyCancelledException e) {
    return new Result(e.getMessage(), false, StatusCode.CONFLICT);
  }


  /**
   * Handles all other exceptions that are not explicitly handled by other methods.
   * This method is a fallback for any unhandled exceptions in the application.
   * It returns a standardized error response with an HTTP 500 (Internal Server Error) status.
   * @param e The exception instance containing details about the error.
   * @return A {@link Result} object containing a generic error message, a failure flag, and a corresponding status code.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result handleException(Exception e) {
    return new Result("A server internal error occurs" + e.getMessage(), false, StatusCode.INTERNAL_SERVER_ERROR);
  }





}
