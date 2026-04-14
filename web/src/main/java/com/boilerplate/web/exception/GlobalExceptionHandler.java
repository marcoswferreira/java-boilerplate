package com.boilerplate.web.exception;

import com.boilerplate.core.exception.BusinessRuleViolationException;
import com.boilerplate.core.exception.ConflictException;
import com.boilerplate.core.exception.EntityNotFoundException;
import com.boilerplate.core.exception.ExternalServiceException;
import com.boilerplate.core.exception.UnauthorizedOperationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler mapping domain exceptions to standardized HTTP responses.
 *
 * <p>Every error response includes a {@code traceId} extracted from MDC (populated by
 * {@link com.boilerplate.web.filter.MdcFilter}) for cross-system correlation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(BusinessRuleViolationException.class)
  public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
    return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(UnauthorizedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedOperationException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, ex.getErrorCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied", List.of());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(ExternalServiceException.class)
  public ResponseEntity<ErrorResponse> handleExternalServiceError(ExternalServiceException ex) {
    return buildResponse(HttpStatus.BAD_GATEWAY, ex.getErrorCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
    List<FieldViolation> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(f -> new FieldViolation(f.getField(), f.getDefaultMessage()))
            .collect(Collectors.toList());
    return buildResponse(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    List<FieldViolation> details =
        ex.getConstraintViolations().stream()
            .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
            .collect(Collectors.toList());
    return buildResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", "Constraint violation", details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request body", List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    // Do not leak internal details in production
    return buildResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "An unexpected error occurred. Please contact support.",
        List.of());
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      HttpStatus status, String code, String message, List<FieldViolation> details) {
    var body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            code,
            message,
            details,
            MDC.get("requestId"));
    return ResponseEntity.status(status).body(body);
  }

  /** Standardized error response DTO. */
  public record ErrorResponse(
      Instant timestamp,
      int status,
      String code,
      String message,
      List<FieldViolation> details,
      String traceId) {}

  /** Field-level validation violation. */
  public record FieldViolation(String field, String message) {}
}
