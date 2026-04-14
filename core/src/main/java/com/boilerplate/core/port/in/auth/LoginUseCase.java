package com.boilerplate.core.port.in.auth;

import com.boilerplate.core.port.in.auth.LoginUseCase.LoginCommand;
import com.boilerplate.core.port.in.auth.LoginUseCase.TokenPair;

/**
 * Input port: authenticates a user and returns an access/refresh token pair.
 *
 * <p>Implementation lives in the application module.
 */
public interface LoginUseCase {

  TokenPair execute(LoginCommand command);

  /** Encapsulates the credentials provided by the caller. */
  record LoginCommand(String email, String password, String tenantId) {}

  /** The result returned on successful authentication. */
  record TokenPair(
      String accessToken, String refreshToken, long accessTokenExpiresInSeconds) {}
}
