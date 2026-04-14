package com.boilerplate.application.auth;

import com.boilerplate.core.domain.User;

/**
 * Port within the application layer: generates and validates JWT tokens.
 *
 * <p>Implemented in the infrastructure/security module to keep JWT library dependencies out of the
 * application layer.
 */
public interface JwtTokenService {

  /** Generates a short-lived access token embedding user identity and permissions. */
  String generateAccessToken(User user);

  /** Returns the configured access token TTL in seconds. */
  long getAccessTokenTtlSeconds();
}
