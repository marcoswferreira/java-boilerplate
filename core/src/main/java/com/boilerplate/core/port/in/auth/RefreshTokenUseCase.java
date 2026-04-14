package com.boilerplate.core.port.in.auth;

import com.boilerplate.core.port.in.auth.LoginUseCase.TokenPair;

/**
 * Input port: validates a refresh token and issues a new access token.
 *
 * <p>The refresh token is a UUID stored in Redis. After validation, a new access token is issued
 * but the refresh token is NOT rotated (to reduce Redis write pressure). Rotation can be enabled
 * via configuration.
 */
public interface RefreshTokenUseCase {

  TokenPair execute(RefreshCommand command);

  record RefreshCommand(String refreshToken) {}
}
