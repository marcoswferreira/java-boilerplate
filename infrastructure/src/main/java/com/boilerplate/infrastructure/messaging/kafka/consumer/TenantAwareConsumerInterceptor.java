package com.boilerplate.infrastructure.messaging.kafka.consumer;

import com.boilerplate.infrastructure.tenant.TenantContextHolder;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer interceptor that initializes the {@link TenantContextHolder} from the
 * {@code tenantId} message header before the listener method is invoked.
 *
 * <p>Ensures proper schema routing for all database operations performed within message handlers.
 *
 * <p>Spring Kafka 3.2.x {@link RecordInterceptor} API:
 * <ul>
 *   <li>{@code intercept(record, consumer)} — called before listener, must return the record
 *   <li>{@code afterRecord(record, consumer)} — always called after listener (success or failure)
 *   <li>{@code success(record, consumer)} / {@code failure(record, ex, consumer)} — optional hooks
 * </ul>
 */
@Component
public class TenantAwareConsumerInterceptor implements RecordInterceptor<String, String> {

  private static final Logger log = LoggerFactory.getLogger(TenantAwareConsumerInterceptor.class);
  private static final String HEADER_TENANT_ID = "tenantId";

  @Override
  public ConsumerRecord<String, String> intercept(
      ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
    var tenantHeader = record.headers().lastHeader(HEADER_TENANT_ID);
    if (tenantHeader != null) {
      String tenantId = new String(tenantHeader.value(), StandardCharsets.UTF_8);
      TenantContextHolder.set(tenantId);
      log.debug(
          "Set tenant context '{}' from Kafka header (topic={}, partition={}, offset={})",
          tenantId, record.topic(), record.partition(), record.offset());
    } else {
      log.warn("Kafka message missing tenantId header (topic={}, partition={}, offset={})",
          record.topic(), record.partition(), record.offset());
    }
    return record;
  }

  /** Always called after listener execution — clears tenant context regardless of outcome. */
  @Override
  public void afterRecord(ConsumerRecord<String, String> record, Consumer<String, String> consumer) {
    TenantContextHolder.clear();
  }
}
