package com.boilerplate.core.port.out;

import com.boilerplate.core.domain.event.DomainEvent;

/**
 * Output port: publishes domain events to the messaging infrastructure.
 *
 * <p>In the standard flow, events are persisted to the outbox table first (within the business
 * transaction) and then relayed to Kafka by the OutboxRelay. This port may also be used for
 * direct publishing in non-critical paths.
 */
public interface EventPublisher {

  void publish(DomainEvent event);
}
