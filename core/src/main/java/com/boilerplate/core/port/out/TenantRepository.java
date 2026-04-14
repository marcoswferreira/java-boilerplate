package com.boilerplate.core.port.out;

import com.boilerplate.core.domain.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Output port: persistence operations for {@link Tenant} aggregates. */
public interface TenantRepository {

  Optional<Tenant> findById(UUID id);

  Optional<Tenant> findBySchemaName(String schemaName);

  List<Tenant> findAllActive();

  Tenant save(Tenant tenant);

  boolean existsBySchemaName(String schemaName);
}
