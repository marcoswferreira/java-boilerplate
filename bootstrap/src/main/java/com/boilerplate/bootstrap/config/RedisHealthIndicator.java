package com.boilerplate.bootstrap.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/** Custom Redis health indicator for Spring Actuator. */
@Component("redis")
public class RedisHealthIndicator implements HealthIndicator {

  private final RedisConnectionFactory connectionFactory;

  public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  @Override
  public Health health() {
    try (var conn = connectionFactory.getConnection()) {
      String pong = conn.ping();
      return Health.up().withDetail("ping", pong).build();
    } catch (Exception e) {
      return Health.down(e).withDetail("error", e.getMessage()).build();
    }
  }
}
