package com.boilerplate.infrastructure.messaging.kafka.outbox;

import com.boilerplate.core.domain.event.DomainEvent;
import com.boilerplate.core.port.out.OutboxEventStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists domain events to the {@code outbox_events} table <em>within the same database
 * transaction</em> as the business operation. This eliminates the dual-write problem.
 */
@Component
public class OutboxEventStoreAdapter implements OutboxEventStore {

  private final OutboxEventRepository repository;
  private final ObjectMapper objectMapper;

  public OutboxEventStoreAdapter(OutboxEventRepository repository, ObjectMapper objectMapper) {
    this.repository = repository;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void save(DomainEvent event) {
    try {
      var entity = new OutboxEventEntity();
      entity.setEventType(event.getEventType());
      entity.setAggregateId(event.getTenantId().toString());
      entity.setTenantId(event.getTenantId().toString());
      entity.setCorrelationId(event.getCorrelationId());
      entity.setPayload(objectMapper.writeValueAsString(event));
      entity.setStatus(OutboxEventEntity.Status.PENDING);
      repository.save(entity);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event for outbox: " + event.getEventId(), e);
    }
  }
}
