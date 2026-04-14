package com.boilerplate.infrastructure.security.jwt;

import com.boilerplate.application.auth.JwtTokenService;
import com.boilerplate.core.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Infrastructure implementation of {@link JwtTokenService}.
 *
 * <p>Generates and validates HS256-signed JWTs using the jjwt library. Claims embedded:
 *
 * <ul>
 *   <li>{@code sub} — userId (UUID string)
 *   <li>{@code tenantId} — tenant identifier
 *   <li>{@code roles} — list of role names
 *   <li>{@code permissions} — list of {@code RESOURCE:ACTION} strings
 * </ul>
 */
@Component
public class JwtTokenProvider implements JwtTokenService {

  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

  private static final String CLAIM_TENANT_ID = "tenantId";
  private static final String CLAIM_ROLES = "roles";
  private static final String CLAIM_PERMISSIONS = "permissions";

  private final JwtProperties jwtProperties;
  private final SecretKey signingKey;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.signingKey =
        Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generateAccessToken(User user) {
    var now = new Date();
    var expiry = new Date(now.getTime() + jwtProperties.getAccessTokenTtlMs());

    List<String> roles =
        user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());

    List<String> permissions =
        user.getAllPermissions().stream()
            .map(p -> p.toPermissionString())
            .collect(Collectors.toList());

    return Jwts.builder()
        .subject(user.getId().toString())
        .claim(CLAIM_TENANT_ID, user.getTenantId().toString())
        .claim(CLAIM_ROLES, roles)
        .claim(CLAIM_PERMISSIONS, permissions)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  @Override
  public long getAccessTokenTtlSeconds() {
    return jwtProperties.getAccessTokenTtlMs() / 1000;
  }

  /** Parses and validates the token, returning the claims on success. */
  public Optional<Claims> parseToken(String token) {
    try {
      var claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      return Optional.of(claims);
    } catch (JwtException | IllegalArgumentException e) {
      log.debug("Invalid JWT token: {}", e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<UUID> extractUserId(String token) {
    return parseToken(token).map(c -> UUID.fromString(c.getSubject()));
  }

  public Optional<String> extractTenantId(String token) {
    return parseToken(token).map(c -> c.get(CLAIM_TENANT_ID, String.class));
  }

  @SuppressWarnings("unchecked")
  public List<String> extractPermissions(String token) {
    return parseToken(token)
        .map(c -> (List<String>) c.get(CLAIM_PERMISSIONS, List.class))
        .orElse(List.of());
  }

  public boolean isTokenValid(String token) {
    return parseToken(token).isPresent();
  }
}
