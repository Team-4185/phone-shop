package com.challengeteam.shop.exceptionHandling.exception.order;

public class InvalidOrderStatusTransitionException extends RuntimeException {
  public InvalidOrderStatusTransitionException(String message) {
    super(message);
  }
}
