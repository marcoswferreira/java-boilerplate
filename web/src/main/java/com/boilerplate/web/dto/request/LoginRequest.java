package com.boilerplate.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for POST /auth/login. */
@Schema(description = "Login credentials")
public record LoginRequest(
    @NotBlank @Email
    @Schema(description = "User email address", example = "user@example.com")
    String email,

    @NotBlank @Size(min = 8, max = 72)
    @Schema(description = "User password", example = "SecureP@ssw0rd")
    String password,

    @NotBlank
    @Schema(description = "Tenant identifier (schema name or UUID)", example = "acme")
    String tenantId) {}
