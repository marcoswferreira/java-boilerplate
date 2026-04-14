package com.boilerplate.infrastructure.security.rbac;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Syntactic sugar for {@code @PreAuthorize} with RBAC permission checks.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @RequiresPermission("bet:place")
 * public void placeBet(PlaceBetCommand command) { ... }
 * }</pre>
 *
 * <p>Translates to {@code @PreAuthorize("hasAuthority('PERMISSION_bet:place')")}. The
 * {@code PERMISSION_} prefix aligns with how {@link
 * com.boilerplate.infrastructure.security.filter.JwtAuthenticationFilter} registers authorities.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasAuthority('PERMISSION_' + '{value}')")
public @interface RequiresPermission {

  /**
   * The permission in {@code RESOURCE:ACTION} format, e.g. {@code "bet:place"}.
   *
   * @return the required permission string
   */
  String value();
}
