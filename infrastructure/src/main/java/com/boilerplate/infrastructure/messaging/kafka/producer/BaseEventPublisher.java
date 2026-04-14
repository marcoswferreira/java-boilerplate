package com.boilerplate.infrastructure.messaging.kafka.producer;

import com.boilerplate.core.domain.event.DomainEvent;
import com.boilerplate.core.port.out.EventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Base Kafka event publisher implementing {@link EventPublisher}.
 *
 * <p>Adds mandatory headers to every message: {@code tenantId}, {@code eventType}, {@code
 * eventId}, {@code timestamp}, {@code correlationId}.
 */
@Component
public class BaseEventPublisher implements EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(BaseEventPublisher.class);

  private static final String HEADER_TENANT_ID = "tenantId";
  private static final String HEADER_EVENT_TYPE = "eventType";
  private static final String HEADER_EVENT_ID = "eventId";
  private static final String HEADER_TIMESTAMP = "timestamp";
  private static final String HEADER_CORRELATION_ID = "correlationId";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final KafkaTopicProperties topicProperties;

  public BaseEventPublisher(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      KafkaTopicProperties topicProperties) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topicProperties = topicProperties;
  }

  @Override
  public void publish(DomainEvent event) {
    String topic = topicProperties.resolveTopic(event.getEventType());
    String key = event.getTenantId().toString();

    try {
      String payload = objectMapper.writeValueAsString(event);
      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, payload);

      addHeader(record, HEADER_TENANT_ID, event.getTenantId().toString());
      addHeader(record, HEADER_EVENT_TYPE, event.getEventType());
      addHeader(record, HEADER_EVENT_ID, event.getEventId().toString());
      addHeader(record, HEADER_TIMESTAMP, event.getOccurredAt().toString());
      addHeader(record, HEADER_CORRELATION_ID,
          event.getCorrelationId() != null ? event.getCorrelationId() : "");

      CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(record);
      future.whenComplete(
          (result, ex) -> {
            if (ex != null) {
              log.error("Failed to publish event {} to topic {}: {}", event.getEventId(), topic, ex.getMessage(), ex);
            } else {
              log.debug("Published event {} to topic {} partition {} offset {}",
                  event.getEventId(), topic,
                  result.getRecordMetadata().partition(),
                  result.getRecordMetadata().offset());
            }
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize event " + event.getEventId(), e);
    }
  }

  private void addHeader(ProducerRecord<String, String> record, String key, String value) {
    if (value != null) {
      record.headers().add(new RecordHeader(key, value.getBytes(StandardCharsets.UTF_8)));
    }
  }
}
