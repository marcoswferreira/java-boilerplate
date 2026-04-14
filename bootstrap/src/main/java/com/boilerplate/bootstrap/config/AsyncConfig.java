package com.boilerplate.bootstrap.config;

import com.boilerplate.infrastructure.tenant.TenantAwareTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/** Async executor configuration with tenant context propagation. */
@Configuration
public class AsyncConfig {

  @Bean(name = "tenantAwareExecutor")
  public Executor tenantAwareExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(8);
    executor.setMaxPoolSize(32);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("tenant-async-");
    executor.setTaskDecorator(new TenantAwareTaskDecorator());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }
}
