package com.boilerplate.infrastructure.messaging.kafka.producer;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Maps event types to Kafka topic names.
 *
 * <p>Configured via {@code app.kafka.topics.*} in application properties:
 *
 * <pre>
 * app.kafka.topics:
 *   user.created: boilerplate.user.events
 *   default: boilerplate.domain.events
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicProperties {

  private Map<String, String> topics = Map.of();
  private String defaultTopic = "boilerplate.domain.events";

  /**
   * Resolves the Kafka topic for a given event type. Falls back to the default topic if no
   * specific mapping is configured.
   */
  public String resolveTopic(String eventType) {
    return topics.getOrDefault(eventType, defaultTopic);
  }

  public Map<String, String> getTopics() {
    return topics;
  }

  public void setTopics(Map<String, String> topics) {
    this.topics = topics;
  }

  public String getDefaultTopic() {
    return defaultTopic;
  }

  public void setDefaultTopic(String defaultTopic) {
    this.defaultTopic = defaultTopic;
  }
}
