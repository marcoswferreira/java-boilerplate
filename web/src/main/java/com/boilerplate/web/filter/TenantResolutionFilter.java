package com.boilerplate.web.filter;

import com.boilerplate.infrastructure.security.jwt.JwtTokenProvider;
import com.boilerplate.infrastructure.tenant.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resolves the current tenant via a chain of responsibility:
 *
 * <ol>
 *   <li>{@code X-Tenant-ID} HTTP header (highest priority)
 *   <li>{@code tenantId} claim from JWT Bearer token
 *   <li>HTTP subdomain (e.g. {@code tenant1.api.company.com})
 * </ol>
 *
 * <p>Always clears the {@link TenantContextHolder} in a {@code finally} block.
 */
@Component
@Order(2)
public class TenantResolutionFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);
  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;

  public TenantResolutionFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String tenantId = resolveFromHeader(request);

      if (tenantId == null) {
        tenantId = resolveFromJwt(request);
      }

      if (tenantId == null) {
        tenantId = resolveFromSubdomain(request);
      }

      if (tenantId != null) {
        TenantContextHolder.set(tenantId);
        MDC.put("tenantId", tenantId);
        log.debug("Resolved tenant: {}", tenantId);
      }

      filterChain.doFilter(request, response);
    } finally {
      TenantContextHolder.clear();
      MDC.remove("tenantId");
    }
  }

  private String resolveFromHeader(HttpServletRequest request) {
    String value = request.getHeader(TENANT_HEADER);
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String resolveFromJwt(HttpServletRequest request) {
    String authHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
      String token = authHeader.substring(BEARER_PREFIX.length());
      return jwtTokenProvider.extractTenantId(token).orElse(null);
    }
    return null;
  }

  private String resolveFromSubdomain(HttpServletRequest request) {
    String host = request.getServerName();
    if (host != null && host.contains(".")) {
      String subdomain = host.split("\\.")[0];
      // Exclude well-known non-tenant subdomains
      if (!subdomain.isEmpty() && !subdomain.equals("www") && !subdomain.equals("api")) {
        return subdomain;
      }
    }
    return null;
  }
}
