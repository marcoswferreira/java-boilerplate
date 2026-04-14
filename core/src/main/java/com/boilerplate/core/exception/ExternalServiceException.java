package com.boilerplate.core.exception;

/** Thrown when an external service call fails. Maps to HTTP 502 Bad Gateway. */
public class ExternalServiceException extends DomainException {

  public ExternalServiceException(String serviceName, String message) {
    super("EXTERNAL_SERVICE_ERROR", "External service '" + serviceName + "' failed: " + message);
  }

  public ExternalServiceException(String serviceName, String message, Throwable cause) {
    super("EXTERNAL_SERVICE_ERROR", "External service '" + serviceName + "' failed: " + message, cause);
  }
}
