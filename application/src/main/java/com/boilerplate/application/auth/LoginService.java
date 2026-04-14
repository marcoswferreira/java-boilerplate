package com.boilerplate.application.auth;

import com.boilerplate.core.domain.User;
import com.boilerplate.core.exception.BusinessRuleViolationException;
import com.boilerplate.core.exception.EntityNotFoundException;
import com.boilerplate.core.port.in.auth.LoginUseCase;
import com.boilerplate.core.port.out.TokenStore;
import com.boilerplate.core.port.out.UserRepository;
import java.time.Duration;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service implementing the login use-case. */
@Service
public class LoginService implements LoginUseCase {

  private final UserRepository userRepository;
  private final TokenStore tokenStore;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;

  public LoginService(
      UserRepository userRepository,
      TokenStore tokenStore,
      PasswordEncoder passwordEncoder,
      JwtTokenService jwtTokenService) {
    this.userRepository = userRepository;
    this.tokenStore = tokenStore;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
  }

  @Override
  @Transactional(readOnly = true)
  public TokenPair execute(LoginCommand command) {
    var tenantId = UUID.fromString(command.tenantId());

    User user =
        userRepository
            .findByEmailAndTenantId(command.email(), tenantId)
            .orElseThrow(() -> new EntityNotFoundException("User", command.email()));

    if (!user.isActive()) {
      throw new BusinessRuleViolationException("AUTH_USER_INACTIVE", "User account is inactive");
    }

    if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
      throw new BusinessRuleViolationException("AUTH_INVALID_CREDENTIALS", "Invalid credentials");
    }

    String accessToken = jwtTokenService.generateAccessToken(user);
    String refreshToken = UUID.randomUUID().toString();

    tokenStore.store(refreshToken, user.getId(), command.tenantId(), Duration.ofDays(7));

    return new TokenPair(accessToken, refreshToken, jwtTokenService.getAccessTokenTtlSeconds());
  }
}
