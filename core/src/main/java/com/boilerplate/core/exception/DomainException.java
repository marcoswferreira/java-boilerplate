package com.boilerplate.core.exception;

/** Abstract base for all domain-specific exceptions. */
public abstract class DomainException extends RuntimeException {

  private final String errorCode;

  protected DomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  protected DomainException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
