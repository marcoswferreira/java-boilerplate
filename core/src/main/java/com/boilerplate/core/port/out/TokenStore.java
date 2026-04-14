package com.boilerplate.core.port.out;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port: store and validate refresh tokens.
 *
 * <p>Backed by Redis in the infrastructure layer. Each token entry stores the associated
 * {@code userId} and {@code tenantId} so they can be embedded in the new access token on refresh.
 */
public interface TokenStore {

  /** Stores a new refresh token with the given TTL. */
  void store(String token, UUID userId, String tenantId, Duration ttl);

  /** Returns the userId if the token is valid (exists and not revoked). */
  Optional<UUID> getUserId(String token);

  /** Returns the tenantId if the token is valid. */
  Optional<String> getTenantId(String token);

  /** Revokes a refresh token immediately (adds to blacklist). */
  void revoke(String token);

  /** Returns true if the token exists and has not been revoked. */
  boolean isValid(String token);
}
