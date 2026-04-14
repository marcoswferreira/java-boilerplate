package com.boilerplate.infrastructure.security.filter;

import com.boilerplate.infrastructure.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts the JWT from the {@code Authorization: Bearer <token>} header, validates it, and
 * populates the {@link SecurityContextHolder} for the current request.
 *
 * <p>Runs once per request. Does nothing if no token is present (allows public endpoints through).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String PERMISSION_PREFIX = "PERMISSION_";

  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null && jwtTokenProvider.isTokenValid(token)) {
      var userId = jwtTokenProvider.extractUserId(token);
      List<String> permissions = jwtTokenProvider.extractPermissions(token);

      if (userId.isPresent()) {
        List<SimpleGrantedAuthority> authorities =
            permissions.stream()
                .map(p -> new SimpleGrantedAuthority(PERMISSION_PREFIX + p))
                .collect(Collectors.toList());

        var authentication =
            new UsernamePasswordAuthenticationToken(userId.get().toString(), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
      return header.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
