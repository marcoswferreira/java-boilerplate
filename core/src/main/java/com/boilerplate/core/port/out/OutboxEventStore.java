package com.boilerplate.core.port.out;

import com.boilerplate.core.domain.event.DomainEvent;

/**
 * Output port: persists a domain event within the same database transaction as the business
 * operation (Outbox Pattern). A separate relay job publishes the event to Kafka.
 */
public interface OutboxEventStore {

  void save(DomainEvent event);
}
