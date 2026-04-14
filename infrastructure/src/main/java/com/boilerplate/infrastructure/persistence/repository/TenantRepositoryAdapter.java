package com.boilerplate.infrastructure.persistence.repository;

import com.boilerplate.core.domain.Tenant;
import com.boilerplate.core.port.out.TenantRepository;
import com.boilerplate.infrastructure.persistence.entity.TenantJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/** JPA adapter implementing the {@link TenantRepository} domain port. */
@Repository
public class TenantRepositoryAdapter implements TenantRepository {

  private final TenantJpaRepository jpaRepository;

  public TenantRepositoryAdapter(TenantJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Tenant> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Tenant> findBySchemaName(String schemaName) {
    return jpaRepository.findBySchemaName(schemaName).map(this::toDomain);
  }

  @Override
  public List<Tenant> findAllActive() {
    return jpaRepository.findAllByActiveTrue().stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public Tenant save(Tenant tenant) {
    return toDomain(jpaRepository.save(toEntity(tenant)));
  }

  @Override
  public boolean existsBySchemaName(String schemaName) {
    return jpaRepository.existsBySchemaName(schemaName);
  }

  private Tenant toDomain(TenantJpaEntity entity) {
    var tenant = Tenant.create(entity.getName());
    if (!entity.isActive()) tenant.deactivate();
    return tenant;
  }

  private TenantJpaEntity toEntity(Tenant tenant) {
    var entity = new TenantJpaEntity();
    entity.setName(tenant.getName());
    entity.setSchemaName(tenant.getSchemaName());
    entity.setActive(tenant.isActive());
    return entity;
  }
}
