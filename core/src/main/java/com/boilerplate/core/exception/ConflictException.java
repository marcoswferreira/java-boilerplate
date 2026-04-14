package com.boilerplate.core.exception;

/** Thrown when an operation conflicts with the current state of a resource. Maps to HTTP 409. */
public class ConflictException extends DomainException {

  public ConflictException(String message) {
    super("CONFLICT", message);
  }

  public ConflictException(String errorCode, String message) {
    super(errorCode, message);
  }
}
