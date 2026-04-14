package com.boilerplate.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** JPA entity mapping the {@code roles} table within a tenant schema. */
@Entity
@Table(name = "roles")
public class RoleJpaEntity extends BaseEntity {

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_role_id")
  private RoleJpaEntity parent;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "role_id")
  private Set<PermissionJpaEntity> permissions = new HashSet<>();

  public RoleJpaEntity() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public RoleJpaEntity getParent() {
    return parent;
  }

  public void setParent(RoleJpaEntity parent) {
    this.parent = parent;
  }

  public Set<PermissionJpaEntity> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<PermissionJpaEntity> permissions) {
    this.permissions = permissions;
  }
}
