package com.boilerplate.infrastructure.tenant;

import com.boilerplate.core.domain.Tenant;
import com.boilerplate.core.port.out.TenantRepository;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * Initializes the {@link TenantAwareDataSource} at application startup.
 *
 * <p>For each active tenant, it:
 * <ol>
 *   <li>Creates the PostgreSQL schema if it does not exist.
 *   <li>Registers a {@link DataSource} with the appropriate {@code search_path}.
 *   <li>Wires all tenant data sources into the {@link TenantAwareDataSource} lookup map.
 * </ol>
 *
 * <p>{@code @Lazy} on TenantRepository breaks the potential circular dependency:
 * DataSource → JPA → TenantRepository → TenantSchemaInitializer → DataSource.
 */
@Component
public class TenantSchemaInitializer implements SmartInitializingSingleton {

  private static final Logger log = LoggerFactory.getLogger(TenantSchemaInitializer.class);

  private final TenantRepository tenantRepository;
  private final TenantAwareDataSource tenantAwareDataSource;
  private final DataSourceProperties dataSourceProperties;

  public TenantSchemaInitializer(
      @Lazy TenantRepository tenantRepository,
      TenantAwareDataSource tenantAwareDataSource,
      DataSourceProperties dataSourceProperties) {
    this.tenantRepository = tenantRepository;
    this.tenantAwareDataSource = tenantAwareDataSource;
    this.dataSourceProperties = dataSourceProperties;
  }

  @Override
  public void afterSingletonsInstantiated() {
    List<Tenant> tenants;
    try {
      tenants = tenantRepository.findAllActive();
    } catch (Exception e) {
      log.warn("Could not load tenants at startup (empty database?): {}", e.getMessage());
      tenants = List.of();
    }

    log.info("Initializing {} tenant schemas", tenants.size());

    Map<Object, Object> targetDataSources = new HashMap<>();
    DataSource defaultDataSource = buildDataSource("public");
    targetDataSources.put("public", defaultDataSource);

    for (Tenant tenant : tenants) {
      String schema = tenant.getSchemaName();
      try {
        ensureSchemaExists(defaultDataSource, schema);
        DataSource tenantDs = buildDataSource(schema);
        targetDataSources.put(schema, tenantDs);
        log.info("Registered tenant schema: {}", schema);
      } catch (Exception e) {
        log.error("Failed to initialize tenant schema '{}': {}", schema, e.getMessage(), e);
      }
    }

    tenantAwareDataSource.setTargetDataSources(targetDataSources);
    tenantAwareDataSource.setDefaultTargetDataSource(defaultDataSource);
    tenantAwareDataSource.afterPropertiesSet();
  }

  private void ensureSchemaExists(DataSource ds, String schema) throws Exception {
    try (Connection conn = ds.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");
    }
  }

  private DataSource buildDataSource(String schema) {
    var ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    // Build URL with currentSchema — handle existing query params gracefully
    String baseUrl = dataSourceProperties.getUrl();
    String url =
        baseUrl.contains("?")
            ? baseUrl + "&currentSchema=" + schema
            : baseUrl + "?currentSchema=" + schema;
    ds.setUrl(url);
    ds.setUsername(dataSourceProperties.getUsername());
    ds.setPassword(dataSourceProperties.getPassword());
    return ds;
  }
}
