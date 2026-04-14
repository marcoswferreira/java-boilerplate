package com.boilerplate.core.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Core domain entity representing an authenticated user within a tenant. */
public class User {

  private UUID id;
  private String email;
  private String passwordHash;
  private UUID tenantId;
  private Set<Role> roles;
  private boolean active;
  private Instant createdAt;
  private Instant updatedAt;

  private User() {}

  public static User create(String email, String passwordHash, UUID tenantId) {
    var user = new User();
    user.id = UUID.randomUUID();
    user.email = email;
    user.passwordHash = passwordHash;
    user.tenantId = tenantId;
    user.roles = new HashSet<>();
    user.active = true;
    user.createdAt = Instant.now();
    user.updatedAt = Instant.now();
    return user;
  }

  public void deactivate() {
    this.active = false;
    this.updatedAt = Instant.now();
  }

  public void activate() {
    this.active = true;
    this.updatedAt = Instant.now();
  }

  public void addRole(Role role) {
    this.roles.add(role);
    this.updatedAt = Instant.now();
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
    this.updatedAt = Instant.now();
  }

  /** Collects all permissions from all roles assigned to this user. */
  public Set<Permission> getAllPermissions() {
    var permissions = new HashSet<Permission>();
    for (Role role : roles) {
      permissions.addAll(role.getPermissions());
    }
    return permissions;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public Set<Role> getRoles() {
    return Set.copyOf(roles);
  }

  public boolean isActive() {
    return active;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
