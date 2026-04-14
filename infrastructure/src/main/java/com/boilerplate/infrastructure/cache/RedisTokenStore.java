package com.boilerplate.infrastructure.cache;

import com.boilerplate.core.port.out.TokenStore;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of {@link TokenStore} using the refresh token blacklist pattern.
 *
 * <p>Key structure:
 * <ul>
 *   <li>{@code rt:{token}} → userId (String)
 *   <li>{@code rt:{token}:tid} → tenantId (String)
 *   <li>{@code rtbl:{token}} → "revoked" (blacklist entry, short TTL)
 * </ul>
 */
@Component
public class RedisTokenStore implements TokenStore {

  private static final String TOKEN_PREFIX = "rt:";
  private static final String TENANT_SUFFIX = ":tid";
  private static final String BLACKLIST_PREFIX = "rtbl:";

  private final CacheService cacheService;

  public RedisTokenStore(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @Override
  public void store(String token, UUID userId, String tenantId, Duration ttl) {
    cacheService.set(TOKEN_PREFIX + token, userId.toString(), ttl);
    cacheService.set(TOKEN_PREFIX + token + TENANT_SUFFIX, tenantId, ttl);
  }

  @Override
  public Optional<UUID> getUserId(String token) {
    return cacheService
        .get(TOKEN_PREFIX + token)
        .map(UUID::fromString);
  }

  @Override
  public Optional<String> getTenantId(String token) {
    return cacheService.get(TOKEN_PREFIX + token + TENANT_SUFFIX);
  }

  @Override
  public void revoke(String token) {
    cacheService.delete(TOKEN_PREFIX + token);
    cacheService.delete(TOKEN_PREFIX + token + TENANT_SUFFIX);
    // Blacklist for 7 days to prevent reuse of revoked tokens
    cacheService.set(BLACKLIST_PREFIX + token, "revoked", Duration.ofDays(7));
  }

  @Override
  public boolean isValid(String token) {
    if (cacheService.exists(BLACKLIST_PREFIX + token)) {
      return false;
    }
    return cacheService.exists(TOKEN_PREFIX + token);
  }
}
