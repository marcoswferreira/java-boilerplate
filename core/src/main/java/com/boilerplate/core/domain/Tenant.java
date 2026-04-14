package com.boilerplate.core.domain;

import java.time.Instant;
import java.util.UUID;

/** Core domain entity representing a tenant in the multi-tenant system. */
public class Tenant {

  private UUID id;
  private String name;
  private String schemaName;
  private boolean active;
  private Instant createdAt;

  private Tenant() {}

  public static Tenant create(String name) {
    validateNotBlank(name, "name");
    var tenant = new Tenant();
    tenant.id = UUID.randomUUID();
    tenant.name = name;
    tenant.schemaName = sanitizeSchemaName(name);
    tenant.active = true;
    tenant.createdAt = Instant.now();
    return tenant;
  }

  /**
   * Sanitizes a tenant name into a valid PostgreSQL schema identifier.
   * Converts to lowercase, replaces spaces/hyphens with underscores.
   */
  public static String sanitizeSchemaName(String name) {
    return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
  }

  public void deactivate() {
    this.active = false;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static void validateNotBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
  }
}
