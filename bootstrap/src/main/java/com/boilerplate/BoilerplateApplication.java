package com.boilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Boilerplate API — Spring Boot entry point.
 *
 * <p>This is the composition root. All application modules are assembled here.
 */
@SpringBootApplication(
    scanBasePackages = {
      "com.boilerplate.application",
      "com.boilerplate.infrastructure",
      "com.boilerplate.web",
      "com.boilerplate.bootstrap"
    })
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@EnableKafka
@ConfigurationPropertiesScan(
    basePackages = {
      "com.boilerplate.infrastructure.security.jwt",
      "com.boilerplate.infrastructure.messaging.kafka.producer",
      "com.boilerplate.bootstrap.config"
    })
public class BoilerplateApplication {

  public static void main(String[] args) {
    SpringApplication.run(BoilerplateApplication.class, args);
  }
}
