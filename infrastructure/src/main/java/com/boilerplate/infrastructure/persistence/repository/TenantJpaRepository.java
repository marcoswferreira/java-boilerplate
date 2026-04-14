package com.boilerplate.infrastructure.persistence.repository;

import com.boilerplate.infrastructure.persistence.entity.TenantJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for tenant persistence in the public schema. */
public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {

  Optional<TenantJpaEntity> findBySchemaName(String schemaName);

  List<TenantJpaEntity> findAllByActiveTrue();

  boolean existsBySchemaName(String schemaName);
}
