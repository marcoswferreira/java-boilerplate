package com.boilerplate.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Populates the SLF4J MDC with request-scoped tracing fields on every HTTP request:
 *
 * <ul>
 *   <li>{@code requestId} — UUID generated per request (used as {@code traceId} in error responses)
 *   <li>{@code tenantId} — from {@link com.boilerplate.infrastructure.tenant.TenantContextHolder}
 *   <li>{@code userId} — from the Spring Security authentication principal
 * </ul>
 *
 * <p>MDC is always cleared in a {@code finally} block to prevent leakage across thread pool reuse.
 */
@Component
@Order(1)
public class MdcFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String requestId = UUID.randomUUID().toString();
      MDC.put("requestId", requestId);
      response.setHeader("X-Request-Id", requestId);

      // Add userId if authenticated (populated after JwtAuthenticationFilter runs)
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
        MDC.put("userId", auth.getPrincipal().toString());
      }

      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
