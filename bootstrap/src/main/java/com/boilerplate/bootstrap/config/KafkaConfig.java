package com.boilerplate.bootstrap.config;

import com.boilerplate.infrastructure.messaging.kafka.consumer.TenantAwareConsumerInterceptor;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Kafka producer and consumer configuration.
 *
 * <p>Producer: retries with exponential backoff, String key/value serializers, idempotent mode.
 * Consumer: tenant-aware interceptor, DLQ via {@link DeadLetterPublishingRecoverer}.
 *
 * <p>Note: {@code buildProducerProperties()} returns an unmodifiable map in Spring Boot 3.x,
 * so we copy it into a mutable HashMap before adding overrides.
 */
@Configuration
public class KafkaConfig {

  private final KafkaProperties kafkaProperties;
  private final TenantAwareConsumerInterceptor tenantAwareConsumerInterceptor;

  public KafkaConfig(
      KafkaProperties kafkaProperties,
      TenantAwareConsumerInterceptor tenantAwareConsumerInterceptor) {
    this.kafkaProperties = kafkaProperties;
    this.tenantAwareConsumerInterceptor = tenantAwareConsumerInterceptor;
  }

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    // Copy to mutable map — Spring Boot 3.x returns unmodifiable map from buildProducerProperties
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.RETRIES_CONFIG, 3);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    // Copy to mutable map — Spring Boot 3.x returns unmodifiable map from buildConsumerProperties
    Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      KafkaTemplate<String, String> kafkaTemplate) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
    factory.setConsumerFactory(consumerFactory());
    factory.setRecordInterceptor(tenantAwareConsumerInterceptor);

    // DLQ: messages that fail after retries go to {topic}.DLT
    var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
    var backOff = new ExponentialBackOff(1000L, 2.0);
    backOff.setMaxAttempts(3);
    factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backOff));

    return factory;
  }
}
