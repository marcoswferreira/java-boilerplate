package com.boilerplate.infrastructure.persistence.repository;

import com.boilerplate.infrastructure.persistence.entity.UserJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for user persistence within a tenant schema. */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

  Optional<UserJpaEntity> findByEmail(String email);

  @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.tenantId = :tenantId")
  Optional<UserJpaEntity> findByEmailAndTenantId(
      @Param("email") String email, @Param("tenantId") UUID tenantId);

  boolean existsByEmail(String email);
}
