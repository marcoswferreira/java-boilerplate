package com.boilerplate.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** JPA entity mapping the {@code users} table within a tenant schema. */
@Entity
@Table(name = "users")
public class UserJpaEntity extends BaseEntity {

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "active", nullable = false)
  private boolean active;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<RoleJpaEntity> roles = new HashSet<>();

  protected UserJpaEntity() {}

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<RoleJpaEntity> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleJpaEntity> roles) {
    this.roles = roles;
  }
}
