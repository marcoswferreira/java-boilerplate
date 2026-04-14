package com.boilerplate.core.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Marker interface for all domain events. Every event must carry tracing metadata. */
public interface DomainEvent {

  /** Unique identifier of this specific event instance. */
  UUID getEventId();

  /** Discriminator string used for routing and deserialization (e.g. {@code user.created}). */
  String getEventType();

  /** The tenant that owns the aggregate that produced this event. */
  UUID getTenantId();

  /** Wall-clock time when the event was raised. */
  Instant getOccurredAt();

  /**
   * Optional correlation identifier for distributed tracing. Propagated from the originating
   * request's MDC {@code requestId}.
   */
  String getCorrelationId();
}
