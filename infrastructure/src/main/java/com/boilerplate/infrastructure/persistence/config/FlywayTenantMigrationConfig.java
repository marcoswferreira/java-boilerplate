package com.boilerplate.infrastructure.persistence.config;

import com.boilerplate.core.domain.Tenant;
import com.boilerplate.core.port.out.TenantRepository;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configures Flyway to run two separate migration sets:
 *
 * <ol>
 *   <li><strong>Public schema</strong> — {@code classpath:db/migration} (prefix {@code V}).
 *       Contains the {@code tenants} table and shared infrastructure.
 *   <li><strong>Per-tenant schema</strong> — {@code classpath:db/tenant-migration} (prefix {@code
 *       T}). Applied to every active tenant schema at startup. New tenants get migrations applied
 *       via {@link #migrateNewTenant}.
 * </ol>
 */
@Configuration
public class FlywayTenantMigrationConfig {

  private static final Logger log = LoggerFactory.getLogger(FlywayTenantMigrationConfig.class);

  private final DataSourceProperties dataSourceProperties;

  public FlywayTenantMigrationConfig(DataSourceProperties dataSourceProperties) {
    this.dataSourceProperties = dataSourceProperties;
  }

  /**
   * Migrates the public schema. Spring Boot's autoconfigure is disabled for Flyway so we
   * control the baseline and location explicitly.
   */
  @Bean(initMethod = "migrate")
  public Flyway publicSchemaFlyway(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .schemas("public")
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .sqlMigrationPrefix("V")
        .load();
  }

  /**
   * Migrates all active tenant schemas. Runs after {@link #publicSchemaFlyway} to ensure the
   * tenants table is available for querying.
   */
  @Bean
  @DependsOn("publicSchemaFlyway")
  public FlywayTenantMigrator tenantMigrator(TenantRepository tenantRepository) {
    return new FlywayTenantMigrator(tenantRepository, dataSourceProperties);
  }

  /** Utility used by tests and tenant-provisioning to migrate a single tenant schema on demand. */
  public static void migrateNewTenant(DataSourceProperties props, String schemaName) {
    String url = props.getUrl() + "?currentSchema=" + schemaName;
    Flyway.configure()
        .dataSource(url, props.getUsername(), props.getPassword())
        .schemas(schemaName)
        .locations("classpath:db/tenant-migration")
        .baselineOnMigrate(true)
        .sqlMigrationPrefix("T")
        .load()
        .migrate();
    log.info("Flyway tenant migration completed for schema '{}'", schemaName);
  }

  /** Runs tenant migrations at startup for all active tenants. */
  public static class FlywayTenantMigrator {

    private final TenantRepository tenantRepository;
    private final DataSourceProperties dataSourceProperties;

    public FlywayTenantMigrator(
        TenantRepository tenantRepository, DataSourceProperties dataSourceProperties) {
      this.tenantRepository = tenantRepository;
      this.dataSourceProperties = dataSourceProperties;
    }

    public void migrateAll() {
      List<Tenant> tenants = tenantRepository.findAllActive();
      log.info("Running Flyway tenant migrations for {} active tenants", tenants.size());
      for (Tenant tenant : tenants) {
        try {
          migrateNewTenant(dataSourceProperties, tenant.getSchemaName());
        } catch (Exception e) {
          log.error("Tenant migration failed for '{}': {}", tenant.getSchemaName(), e.getMessage(), e);
        }
      }
    }
  }
}
