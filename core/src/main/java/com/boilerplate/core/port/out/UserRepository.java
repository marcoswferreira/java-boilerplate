package com.boilerplate.core.port.out;

import com.boilerplate.core.domain.User;
import java.util.Optional;
import java.util.UUID;

/** Output port: persistence operations for {@link User} aggregates. */
public interface UserRepository {

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

  User save(User user);

  void delete(UUID id);

  boolean existsByEmail(String email);
}
