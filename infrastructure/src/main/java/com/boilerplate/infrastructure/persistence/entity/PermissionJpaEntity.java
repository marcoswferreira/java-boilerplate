package com.boilerplate.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** JPA entity mapping the {@code permissions} table within a tenant schema. */
@Entity
@Table(name = "permissions")
public class PermissionJpaEntity extends BaseEntity {

  @Column(name = "resource", nullable = false, length = 100)
  private String resource;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  public PermissionJpaEntity() {}

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
