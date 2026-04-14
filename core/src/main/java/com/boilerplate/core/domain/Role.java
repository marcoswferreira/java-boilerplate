package com.boilerplate.core.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Core domain entity representing a role within a tenant's RBAC model. */
public class Role {

  private UUID id;
  private String name;
  private UUID tenantId;
  private Set<Permission> permissions;
  private Role parent; // for hierarchical roles

  private Role() {}

  public static Role create(String name, UUID tenantId) {
    var role = new Role();
    role.id = UUID.randomUUID();
    role.name = name;
    role.tenantId = tenantId;
    role.permissions = new HashSet<>();
    return role;
  }

  public void addPermission(Permission permission) {
    this.permissions.add(permission);
  }

  public void removePermission(Permission permission) {
    this.permissions.remove(permission);
  }

  public void setParent(Role parent) {
    this.parent = parent;
  }

  /**
   * Collects all permissions including those inherited from parent roles (hierarchical RBAC).
   * Recursion terminates when there is no parent role.
   */
  public Set<Permission> getPermissions() {
    var all = new HashSet<>(permissions);
    if (parent != null) {
      all.addAll(parent.getPermissions());
    }
    return Set.copyOf(all);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public Role getParent() {
    return parent;
  }
}
