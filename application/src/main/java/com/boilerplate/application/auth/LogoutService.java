package com.boilerplate.application.auth;

import com.boilerplate.core.port.in.auth.LogoutUseCase;
import com.boilerplate.core.port.out.TokenStore;
import org.springframework.stereotype.Service;

/** Application service implementing the logout use-case. */
@Service
public class LogoutService implements LogoutUseCase {

  private final TokenStore tokenStore;

  public LogoutService(TokenStore tokenStore) {
    this.tokenStore = tokenStore;
  }

  @Override
  public void execute(LogoutCommand command) {
    // Revoke is idempotent — safe to call even if already revoked
    tokenStore.revoke(command.refreshToken());
  }
}
