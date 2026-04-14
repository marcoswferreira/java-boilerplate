package com.boilerplate.infrastructure.messaging.kafka.outbox;

import com.boilerplate.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity for the outbox pattern. Stored in each tenant schema. Events are persisted
 * transactionally with business operations, then relayed to Kafka by {@link OutboxRelay}.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity extends BaseEntity {

  public enum Status {
    PENDING,
    PUBLISHED,
    FAILED
  }

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "aggregate_id", nullable = false, length = 36)
  private String aggregateId;

  @Column(name = "tenant_id", nullable = false, length = 36)
  private String tenantId;

  @Column(name = "correlation_id", length = 36)
  private String correlationId;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private Status status = Status.PENDING;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  protected OutboxEventEntity() {}

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(String aggregateId) {
    this.aggregateId = aggregateId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
