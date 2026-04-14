package com.boilerplate.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User domain entity")
class UserTest {

  @Test
  @DisplayName("should create active user with given credentials")
  void shouldCreateUser() {
    UUID tenantId = UUID.randomUUID();
    User user = User.create("alice@example.com", "hashed_pw", tenantId);

    assertThat(user.getId()).isNotNull();
    assertThat(user.getEmail()).isEqualTo("alice@example.com");
    assertThat(user.getPasswordHash()).isEqualTo("hashed_pw");
    assertThat(user.getTenantId()).isEqualTo(tenantId);
    assertThat(user.isActive()).isTrue();
    assertThat(user.getRoles()).isEmpty();
    assertThat(user.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("should collect all permissions from all roles (flattened)")
  void shouldCollectAllPermissions() {
    UUID tenantId = UUID.randomUUID();
    User user = User.create("bob@example.com", "pw", tenantId);

    Role admin = Role.create("ADMIN", tenantId);
    admin.addPermission(Permission.of("user", "read"));
    admin.addPermission(Permission.of("user", "write"));

    Role operator = Role.create("OPERATOR", tenantId);
    operator.addPermission(Permission.of("wallet", "view"));

    user.addRole(admin);
    user.addRole(operator);

    assertThat(user.getAllPermissions())
        .containsExactlyInAnyOrder(
            Permission.of("user", "read"),
            Permission.of("user", "write"),
            Permission.of("wallet", "view"));
  }

  @Test
  @DisplayName("should deactivate and reactivate user")
  void shouldToggleActiveState() {
    User user = User.create("x@y.com", "pw", UUID.randomUUID());

    user.deactivate();
    assertThat(user.isActive()).isFalse();

    user.activate();
    assertThat(user.isActive()).isTrue();
  }

  @Test
  @DisplayName("should inherit permissions from parent role (hierarchical RBAC)")
  void shouldInheritPermissionsFromParentRole() {
    UUID tenantId = UUID.randomUUID();

    Role operator = Role.create("OPERATOR", tenantId);
    operator.addPermission(Permission.of("bet", "view"));

    Role admin = Role.create("ADMIN", tenantId);
    admin.setParent(operator);
    admin.addPermission(Permission.of("bet", "cancel"));

    User user = User.create("admin@example.com", "pw", tenantId);
    user.addRole(admin);

    // Admin role inherits bet:view from OPERATOR via parent
    assertThat(user.getAllPermissions())
        .contains(Permission.of("bet", "view"), Permission.of("bet", "cancel"));
  }
}
