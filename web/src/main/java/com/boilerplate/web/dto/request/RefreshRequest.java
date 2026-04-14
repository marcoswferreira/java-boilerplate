package com.boilerplate.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body for POST /auth/refresh. */
@Schema(description = "Refresh token payload")
public record RefreshRequest(
    @NotBlank
    @Schema(description = "Opaque refresh token received from /auth/login", example = "550e8400-e29b-41d4-a716-446655440000")
    String refreshToken) {}
