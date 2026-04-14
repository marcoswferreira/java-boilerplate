package com.boilerplate.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** JPA entity for the {@code tenants} table in the public schema. */
@Entity
@Table(name = "tenants", schema = "public")
public class TenantJpaEntity extends BaseEntity {

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "schema_name", nullable = false, unique = true, length = 100)
  private String schemaName;

  @Column(name = "active", nullable = false)
  private boolean active;

  protected TenantJpaEntity() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
