package com.boilerplate.core.exception;

/** Thrown when a business rule is violated. Maps to HTTP 422 Unprocessable Entity. */
public class BusinessRuleViolationException extends DomainException {

  public BusinessRuleViolationException(String message) {
    super("BUSINESS_RULE_VIOLATION", message);
  }

  public BusinessRuleViolationException(String errorCode, String message) {
    super(errorCode, message);
  }
}
