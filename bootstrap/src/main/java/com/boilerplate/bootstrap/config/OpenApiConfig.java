package com.boilerplate.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** OpenAPI / Swagger configuration with global JWT Bearer security scheme. */
@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Value("${spring.application.name:boilerplate-api}")
  private String applicationName;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Boilerplate API")
                .description(
                    "Enterprise Java Boilerplate — Multi-tenant, JWT auth, Kafka, Redis")
                .version("1.0.0")
                .contact(new Contact().name("Boilerplate Team").email("team@boilerplate.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.boilerplate.com").description("Production")))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_AUTH,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "JWT access token. Obtain via POST /auth/login and include as: "
                                + "Authorization: Bearer <token>")))
        // Apply JWT auth globally — individual public endpoints override with @SecurityRequirements({})
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
  }
}
