package com.boilerplate.core.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Event emitted when a new {@link com.boilerplate.core.domain.User} is created. */
public record UserCreatedEvent(
    UUID eventId,
    String eventType,
    UUID tenantId,
    Instant occurredAt,
    String correlationId,
    UUID userId,
    String email)
    implements DomainEvent {

  public static UserCreatedEvent of(UUID tenantId, UUID userId, String email, String correlationId) {
    return new UserCreatedEvent(
        UUID.randomUUID(),
        "user.created",
        tenantId,
        Instant.now(),
        correlationId,
        userId,
        email);
  }

  @Override
  public UUID getEventId() {
    return eventId;
  }

  @Override
  public String getEventType() {
    return eventType;
  }

  @Override
  public UUID getTenantId() {
    return tenantId;
  }

  @Override
  public Instant getOccurredAt() {
    return occurredAt;
  }

  @Override
  public String getCorrelationId() {
    return correlationId;
  }
}
