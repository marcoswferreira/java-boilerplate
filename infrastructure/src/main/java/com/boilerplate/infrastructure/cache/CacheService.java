package com.boilerplate.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Generic cache service backed by Redis.
 *
 * <p>Keys are automatically prefixed with {@code {tenant}:{namespace}:} when constructed via
 * {@link #key(String, String, String)}.
 */
@Service
public class CacheService {

  private final RedisTemplate<String, Object> redisTemplate;

  public CacheService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void set(String key, Object value, Duration ttl) {
    redisTemplate.opsForValue().set(key, value, ttl.toSeconds(), TimeUnit.SECONDS);
  }

  public void set(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> get(String key, Class<T> type) {
    Object value = redisTemplate.opsForValue().get(key);
    if (value == null) return Optional.empty();
    return Optional.of(type.cast(value));
  }

  public Optional<String> get(String key) {
    return get(key, String.class);
  }

  public void delete(String key) {
    redisTemplate.delete(key);
  }

  public boolean exists(String key) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  /** Atomically increment a counter and set TTL if this is the first increment. */
  public long increment(String key, Duration ttl) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) {
      redisTemplate.expire(key, ttl);
    }
    return count == null ? 0L : count;
  }

  /**
   * Builds a namespaced key in the format: {@code {tenantId}:{namespace}:{key}}
   *
   * @param tenantId the tenant identifier
   * @param namespace logical namespace (e.g. {@code "tokens"}, {@code "rate-limit"})
   * @param key the specific key
   * @return the composed Redis key
   */
  public static String key(String tenantId, String namespace, String key) {
    return tenantId + ":" + namespace + ":" + key;
  }
}
