package com.boilerplate.core.exception;

/** Thrown when a requested resource does not exist. Maps to HTTP 404 Not Found. */
public class EntityNotFoundException extends DomainException {

  public EntityNotFoundException(String entityName, Object id) {
    super("ENTITY_NOT_FOUND", entityName + " with id '" + id + "' was not found");
  }

  public EntityNotFoundException(String message) {
    super("ENTITY_NOT_FOUND", message);
  }
}
