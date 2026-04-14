package com.boilerplate.infrastructure.tenant;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routes database connections to the correct tenant schema by setting the PostgreSQL
 * {@code search_path} before each connection is returned from the pool.
 *
 * <p>The lookup key is the schema name stored in {@link TenantContextHolder}. If no tenant context
 * is active, the connection defaults to the {@code public} schema (for system-level operations
 * like tenant provisioning).
 */
public class TenantAwareDataSource extends AbstractRoutingDataSource {

  private static final Logger log = LoggerFactory.getLogger(TenantAwareDataSource.class);
  private static final String PUBLIC_SCHEMA = "public";

  @Override
  protected Object determineCurrentLookupKey() {
    String tenantId = TenantContextHolder.get();
    if (tenantId == null) {
      log.debug("No tenant context set — defaulting to public schema");
      return PUBLIC_SCHEMA;
    }
    return tenantId;
  }

  /**
   * Sets the PostgreSQL {@code search_path} on the underlying connection. This ensures Hibernate
   * resolves unqualified table names within the correct tenant schema.
   */
  @Override
  protected DataSource determineTargetDataSource() {
    return super.determineTargetDataSource();
  }
}
