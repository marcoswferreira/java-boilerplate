package com.boilerplate.core.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Core domain entity representing a permission in RESOURCE:ACTION format.
 *
 * <p>Examples: {@code bet:place}, {@code user:read}, {@code wallet:withdraw}
 */
public class Permission {

  private UUID id;
  private String resource;
  private String action;

  private Permission() {}

  public static Permission of(String resource, String action) {
    validateNotBlank(resource, "resource");
    validateNotBlank(action, "action");
    var p = new Permission();
    p.id = UUID.randomUUID();
    p.resource = resource.toLowerCase();
    p.action = action.toLowerCase();
    return p;
  }

  /**
   * Parses a permission from its canonical {@code RESOURCE:ACTION} string form.
   *
   * @param value e.g. {@code "bet:place"}
   */
  public static Permission parse(String value) {
    Objects.requireNonNull(value, "Permission value must not be null");
    String[] parts = value.split(":", 2);
    if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
      throw new IllegalArgumentException(
          "Invalid permission format '" + value + "'. Expected RESOURCE:ACTION");
    }
    return of(parts[0], parts[1]);
  }

  /** Returns the canonical string representation, e.g. {@code bet:place}. */
  public String toPermissionString() {
    return resource + ":" + action;
  }

  private static void validateNotBlank(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
  }

  public UUID getId() {
    return id;
  }

  public String getResource() {
    return resource;
  }

  public String getAction() {
    return action;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Permission p)) return false;
    return Objects.equals(resource, p.resource) && Objects.equals(action, p.action);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resource, action);
  }

  @Override
  public String toString() {
    return toPermissionString();
  }
}
