/**
 *Copyright © 2025
 * @Author = TJTechy (Tajudeen Busari)
 * @Version = 1.0
 * This file is part of product-service module of the Ecommerce Microservices project.
 */
package com.tjtechy.product_service.exception;


import com.tjtechy.Result;
import com.tjtechy.StatusCode;
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

/**
 * This class is used to handle exceptions in the controller layer
 */
@RestControllerAdvice
public class ExceptionHandlingAdvice {

  /**
   * Handles exceptions of type {@link ProductNotFoundException}.
   * <p>
   * This method is triggered whenever a {@code ProductNotFoundException} is thrown within the application.
   * It returns a standardized error response with an HTTP 404 (Not Found) status.
   * </p>
   *
   * @param e The exception instance containing details about the missing product.
   * @return A {@link Result} object containing an error message, a failure flag, and a corresponding status code.
   */
  @ExceptionHandler(ProductNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleProductNotFoundException(ProductNotFoundException e) {
    return new Result(e.getMessage(), false, StatusCode.NOT_FOUND);
  }


  /**
   * <p>Handles validation errors when request data fails validation constraints.
   * When @Valid annotation is used in the controller method request,
   * this method will be triggered if that validation fails.
   * </p>
   * This method catches {@link MethodArgumentNotValidException} which occurs when
   * request body parameters fail validation annotations (e.g., {@code @NotNull, @Size, @Min}).
   * </p>
   * <p>
   * The method extracts validation error details and returns them in a structured format.
   * Each field that caused the error is mapped to its corresponding validation message.
   * </p>
   *
   * @param e The exception that contains validation errors.
   * @return {@link Result} object containing error messages mapped to invalid fields.
   * @see MethodArgumentNotValidException
   * @see FieldError
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Result handleInValidDataException(MethodArgumentNotValidException e) {
    List<ObjectError> fieldErrors = e.getAllErrors();
    Map<String, String> map = new HashMap<>(fieldErrors.size());
    fieldErrors.forEach(error -> {
      String key = ((FieldError)error).getField();
      String value = ((FieldError) error).getDefaultMessage();
      map.put(key, value);
    });

    return new Result("Provided arguments are invalid, see data for details", false, map, StatusCode.BAD_REQUEST);
  }

  /**
   * Handles any unhandled exceptions that occur in the application.
   * <p>
   * This method is a global exception handler that catches all exceptions
   * that are not specifically handled by other exception handlers. It
   * ensures that the application does not expose stack traces or
   * implementation details to the client.
   * </p>
   *
   * <p><b>Response:</b></p>
   * <ul>
   *   <li>HTTP Status: {@code 500 INTERNAL_SERVER_ERROR}</li>
   *   <li>Message: "A server internal error occurs"</li>
   *   <li>Flag: {@code false} (Indicating failure)</li>
   *   <li>Error Details: Exception message</li>
   *   <li>Status Code: {@code StatusCode.INTERNAL_SERVER_ERROR}</li>
   * </ul>
   *
   * @param e The unhandled exception.
   * @return A {@link Result} object containing error details.
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Result handleOtherException(Exception e) {
    return new Result("A server internal error occurs", false, e.getMessage(), StatusCode.INTERNAL_SERVER_ERROR );
  }
}
