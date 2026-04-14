package com.boilerplate.infrastructure.persistence.repository;

import com.boilerplate.core.domain.Permission;
import com.boilerplate.core.domain.Role;
import com.boilerplate.core.domain.User;
import com.boilerplate.core.port.out.UserRepository;
import com.boilerplate.infrastructure.persistence.entity.PermissionJpaEntity;
import com.boilerplate.infrastructure.persistence.entity.RoleJpaEntity;
import com.boilerplate.infrastructure.persistence.entity.UserJpaEntity;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** JPA adapter implementing the {@link UserRepository} domain port. */
@Repository
public class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository jpaRepository;

  public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public Optional<User> findByEmailAndTenantId(String email, UUID tenantId) {
    return jpaRepository.findByEmailAndTenantId(email, tenantId).map(this::toDomain);
  }

  @Override
  public User save(User user) {
    UserJpaEntity entity = toEntity(user);
    return toDomain(jpaRepository.save(entity));
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  // ---- Mapping ----

  private User toDomain(UserJpaEntity entity) {
    var user = User.create(entity.getEmail(), entity.getPasswordHash(), entity.getTenantId());
    entity.getRoles().forEach(r -> user.addRole(roleToDomain(r)));
    if (!entity.isActive()) user.deactivate();
    return user;
  }

  private Role roleToDomain(RoleJpaEntity entity) {
    var role = Role.create(entity.getName(), entity.getTenantId());
    entity.getPermissions().forEach(p -> role.addPermission(permissionToDomain(p)));
    if (entity.getParent() != null) {
      role.setParent(roleToDomain(entity.getParent()));
    }
    return role;
  }

  private Permission permissionToDomain(PermissionJpaEntity entity) {
    return Permission.of(entity.getResource(), entity.getAction());
  }

  private UserJpaEntity toEntity(User user) {
    var entity = new UserJpaEntity();
    entity.setEmail(user.getEmail());
    entity.setPasswordHash(user.getPasswordHash());
    entity.setTenantId(user.getTenantId());
    entity.setActive(user.isActive());
    return entity;
  }
}
