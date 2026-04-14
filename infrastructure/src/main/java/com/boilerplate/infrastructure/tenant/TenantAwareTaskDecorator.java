package com.boilerplate.infrastructure.tenant;

import org.springframework.core.task.TaskDecorator;

/**
 * {@link TaskDecorator} that captures the current tenant context from the submitting thread and
 * restores it in the executing thread.
 *
 * <p>Use this decorator with any {@code ThreadPoolTaskExecutor} or {@code @Async} executor to
 * ensure tenant isolation is preserved across asynchronous boundaries.
 */
public class TenantAwareTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    // Capture the tenant from the calling thread
    String tenantId = TenantContextHolder.get();

    return () -> {
      try {
        if (tenantId != null) {
          TenantContextHolder.set(tenantId);
        }
        runnable.run();
      } finally {
        TenantContextHolder.clear();
      }
    };
  }
}
