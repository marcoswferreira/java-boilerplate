package com.boilerplate.infrastructure.security.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized JWT configuration properties.
 *
 * <p>Bound from {@code app.jwt.*} properties. Validated at startup — the application will fail
 * fast if the secret is too short or TTL values are missing.
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  @NotBlank
  @Size(min = 32, message = "JWT secret must be at least 32 characters (256 bits)")
  private String secret;

  @Min(60_000)
  private long accessTokenTtlMs = 900_000L; // default: 15 minutes

  @Min(3_600_000)
  private long refreshTokenTtlMs = 604_800_000L; // default: 7 days

  private String issuer = "boilerplate-api";

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getAccessTokenTtlMs() {
    return accessTokenTtlMs;
  }

  public void setAccessTokenTtlMs(long accessTokenTtlMs) {
    this.accessTokenTtlMs = accessTokenTtlMs;
  }

  public long getRefreshTokenTtlMs() {
    return refreshTokenTtlMs;
  }

  public void setRefreshTokenTtlMs(long refreshTokenTtlMs) {
    this.refreshTokenTtlMs = refreshTokenTtlMs;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }
}
