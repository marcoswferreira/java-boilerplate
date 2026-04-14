package com.boilerplate.application.auth;

import com.boilerplate.core.domain.User;
import com.boilerplate.core.exception.BusinessRuleViolationException;
import com.boilerplate.core.exception.EntityNotFoundException;
import com.boilerplate.core.port.in.auth.LoginUseCase.TokenPair;
import com.boilerplate.core.port.in.auth.RefreshTokenUseCase;
import com.boilerplate.core.port.out.TokenStore;
import com.boilerplate.core.port.out.UserRepository;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Application service implementing the refresh-token use-case. */
@Service
public class RefreshTokenService implements RefreshTokenUseCase {

  private final TokenStore tokenStore;
  private final UserRepository userRepository;
  private final JwtTokenService jwtTokenService;

  public RefreshTokenService(
      TokenStore tokenStore, UserRepository userRepository, JwtTokenService jwtTokenService) {
    this.tokenStore = tokenStore;
    this.userRepository = userRepository;
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  public TokenPair execute(RefreshCommand command) {
    if (!tokenStore.isValid(command.refreshToken())) {
      throw new BusinessRuleViolationException("AUTH_INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
    }

    UUID userId =
        tokenStore
            .getUserId(command.refreshToken())
            .orElseThrow(
                () -> new BusinessRuleViolationException("AUTH_INVALID_REFRESH_TOKEN", "Refresh token data not found"));

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));

    if (!user.isActive()) {
      tokenStore.revoke(command.refreshToken());
      throw new BusinessRuleViolationException("AUTH_USER_INACTIVE", "User account is inactive");
    }

    String newAccessToken = jwtTokenService.generateAccessToken(user);

    // Rotate refresh token for added security
    String newRefreshToken = UUID.randomUUID().toString();
    String tenantId = tokenStore.getTenantId(command.refreshToken()).orElse(user.getTenantId().toString());
    tokenStore.revoke(command.refreshToken());
    tokenStore.store(newRefreshToken, user.getId(), tenantId, Duration.ofDays(7));

    return new TokenPair(newAccessToken, newRefreshToken, jwtTokenService.getAccessTokenTtlSeconds());
  }
}
