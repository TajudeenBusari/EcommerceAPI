package com.tjtechy.inventory_service.exception;

import com.tjtechy.Result;
import com.tjtechy.StatusCode;
import com.tjtechy.modelNotFoundException.InventoryNotFoundException;
import com.tjtechy.modelNotFoundException.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlingAdvice {

  /**
   * Handles exceptions of type {@link InventoryNotFoundException}.
   * @param exception
   * @return Result {@link Result}
   */
  @ExceptionHandler(InventoryNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleInventoryNotFoundException(InventoryNotFoundException exception) {
    return new Result(exception.getMessage(), false, StatusCode.NOT_FOUND);
  }

  /**
   * Handles exceptions of type {@link ProductNotFoundException}.
   * This needs to be handled here to have a good error format for when product is not found
   * for the findInventoryByProductId method
   * @param exception
   * @return Result {@link Result}
   */
  @ExceptionHandler(ProductNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result handleProductNotFoundException(ProductNotFoundException exception) {
    return new Result(exception.getMessage(), false, StatusCode.NOT_FOUND);
  }




}
