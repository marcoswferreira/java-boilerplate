package com.boilerplate.core.exception;

/** Thrown when the caller does not have permission to perform an operation. Maps to HTTP 403. */
public class UnauthorizedOperationException extends DomainException {

  public UnauthorizedOperationException(String operation) {
    super("UNAUTHORIZED_OPERATION", "Not authorized to perform: " + operation);
  }

  public UnauthorizedOperationException(String errorCode, String message) {
    super(errorCode, message);
  }
}
