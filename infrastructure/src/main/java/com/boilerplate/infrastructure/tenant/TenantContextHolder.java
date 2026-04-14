package com.boilerplate.infrastructure.tenant;

/**
 * Holds the current tenant identifier for the executing thread.
 *
 * <p><strong>Usage contract:</strong> Always call {@link #clear()} in a {@code finally} block to
 * prevent context leakage across thread pool reuse.
 *
 * <pre>{@code
 * TenantContextHolder.set(tenantId);
 * try {
 *   // ... business logic
 * } finally {
 *   TenantContextHolder.clear();
 * }
 * }</pre>
 */
public final class TenantContextHolder {

  private static final ThreadLocal<String> CONTEXT = new InheritableThreadLocal<>();

  private TenantContextHolder() {}

  public static void set(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be blank");
    }
    CONTEXT.set(tenantId);
  }

  public static String get() {
    return CONTEXT.get();
  }

  public static boolean hasContext() {
    return CONTEXT.get() != null;
  }

  public static void clear() {
    CONTEXT.remove();
  }
}
