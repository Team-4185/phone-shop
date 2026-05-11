package com.challengeteam.shop.exceptionHandling.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {
  public InvalidOrderStatusTransitionException(String message) {
    super(message);
  }
}
