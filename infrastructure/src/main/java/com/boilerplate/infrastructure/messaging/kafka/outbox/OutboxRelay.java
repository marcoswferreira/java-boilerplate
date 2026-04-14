package com.boilerplate.infrastructure.messaging.kafka.outbox;

import com.boilerplate.core.domain.Tenant;
import com.boilerplate.core.port.out.TenantRepository;
import com.boilerplate.infrastructure.tenant.TenantContextHolder;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job that reads pending events from the outbox table and publishes them to Kafka.
 *
 * <p>Runs every 5 seconds by default (configurable via {@code app.outbox.relay.interval-ms}).
 * Iterates all active tenants so outbox events from every schema are processed.
 */
@Component
public class OutboxRelay {

  private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

  private final OutboxEventRepository outboxEventRepository;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final TenantRepository tenantRepository;

  public OutboxRelay(
      OutboxEventRepository outboxEventRepository,
      KafkaTemplate<String, String> kafkaTemplate,
      @Lazy TenantRepository tenantRepository) {
    this.outboxEventRepository = outboxEventRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.tenantRepository = tenantRepository;
  }

  @Scheduled(fixedDelayString = "${app.outbox.relay.interval-ms:5000}")
  public void relay() {
    List<Tenant> tenants = tenantRepository.findAllActive();
    for (Tenant tenant : tenants) {
      TenantContextHolder.set(tenant.getSchemaName());
      try {
        relayForTenant();
      } finally {
        TenantContextHolder.clear();
      }
    }
  }

  @Transactional
  protected void relayForTenant() {
    List<OutboxEventEntity> pending = outboxEventRepository.findPending();
    for (OutboxEventEntity event : pending) {
      try {
        kafkaTemplate.send(event.getEventType(), event.getTenantId(), event.getPayload());
        event.setStatus(OutboxEventEntity.Status.PUBLISHED);
        event.setPublishedAt(Instant.now());
        log.debug("Relayed outbox event {} to topic {}", event.getId(), event.getEventType());
      } catch (Exception e) {
        event.setStatus(OutboxEventEntity.Status.FAILED);
        event.setErrorMessage(e.getMessage());
        log.error("Failed to relay outbox event {}: {}", event.getId(), e.getMessage(), e);
      }
      outboxEventRepository.save(event);
    }
  }
}
