package com.boilerplate.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response body for successful authentication or token refresh. */
@Schema(description = "JWT token pair returned on successful authentication")
public record TokenResponse(
    @JsonProperty("access_token")
    @Schema(description = "Short-lived JWT access token (Bearer)")
    String accessToken,

    @JsonProperty("refresh_token")
    @Schema(description = "Long-lived opaque refresh token")
    String refreshToken,

    @JsonProperty("token_type")
    @Schema(description = "Token type — always 'Bearer'", example = "Bearer")
    String tokenType,

    @JsonProperty("expires_in")
    @Schema(description = "Access token lifetime in seconds", example = "900")
    long expiresIn) {

  public static TokenResponse of(String accessToken, String refreshToken, long expiresInSeconds) {
    return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
  }
}
