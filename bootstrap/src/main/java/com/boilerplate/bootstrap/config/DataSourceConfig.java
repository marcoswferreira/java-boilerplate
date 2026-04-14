package com.boilerplate.bootstrap.config;

import com.boilerplate.infrastructure.tenant.TenantAwareDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * DataSource configuration wiring the multi-tenant {@link TenantAwareDataSource}.
 *
 * <p>We exclude Spring Boot's {@link DataSourceAutoConfiguration} because we provide a custom
 * routing datasource. Spring Boot's {@code DataSourceProperties} bean is still present and
 * auto-configured from {@code spring.datasource.*} properties.
 *
 * <p>The {@link TenantAwareDataSource} is registered as the primary {@code DataSource} so all
 * Spring/JPA components use it. At startup, {@link
 * com.boilerplate.infrastructure.tenant.TenantSchemaInitializer} populates its target data
 * sources map with per-tenant connections.
 */
@Configuration
public class DataSourceConfig {

  /**
   * The primary DataSource — a routing datasource that delegates to the correct tenant schema
   * based on the current {@link com.boilerplate.infrastructure.tenant.TenantContextHolder}.
   *
   * <p>Starts empty; populated by {@link
   * com.boilerplate.infrastructure.tenant.TenantSchemaInitializer} during
   * {@code SmartInitializingSingleton#afterSingletonsInstantiated()}.
   */
  @Bean
  @Primary
  public TenantAwareDataSource dataSource() {
    // The routing datasource is intentionally created empty.
    // TenantSchemaInitializer will call setTargetDataSources() + afterPropertiesSet()
    // after all singletons are instantiated.
    return new TenantAwareDataSource();
  }
}
