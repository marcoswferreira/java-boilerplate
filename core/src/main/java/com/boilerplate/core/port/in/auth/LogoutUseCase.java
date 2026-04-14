package com.boilerplate.core.port.in.auth;

/**
 * Input port: revokes a refresh token, effectively logging the user out.
 *
 * <p>The revoked token is added to a Redis blacklist; subsequent refresh attempts with the same
 * token will be rejected.
 */
public interface LogoutUseCase {

  void execute(LogoutCommand command);

  record LogoutCommand(String refreshToken) {}
}
