package com.boilerplate.application.auth;

import com.boilerplate.core.domain.Permission;
import com.boilerplate.core.domain.Role;
import com.boilerplate.core.domain.User;
import com.boilerplate.core.exception.BusinessRuleViolationException;
import com.boilerplate.core.exception.EntityNotFoundException;
import com.boilerplate.core.port.in.auth.LoginUseCase;
import com.boilerplate.core.port.out.TokenStore;
import com.boilerplate.core.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService")
class LoginServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private TokenStore tokenStore;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenService jwtTokenService;

  private LoginService sut;
  private UUID tenantId;
  private User mockUser;

  @BeforeEach
  void setUp() {
    sut = new LoginService(userRepository, tokenStore, passwordEncoder, jwtTokenService);
    tenantId = UUID.randomUUID();
    mockUser = User.create("user@test.com", "hashed_password", tenantId);
    Role adminRole = Role.create("ADMIN", tenantId);
    adminRole.addPermission(Permission.of("user", "read"));
    mockUser.addRole(adminRole);
  }

  @Test
  @DisplayName("should return token pair on valid credentials")
  void shouldReturnTokenPairOnValidCredentials() {
    when(userRepository.findByEmailAndTenantId("user@test.com", tenantId))
        .thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("raw_password", "hashed_password")).thenReturn(true);
    when(jwtTokenService.generateAccessToken(mockUser)).thenReturn("access.token.value");
    when(jwtTokenService.getAccessTokenTtlSeconds()).thenReturn(900L);

    var result = sut.execute(
        new LoginUseCase.LoginCommand("user@test.com", "raw_password", tenantId.toString()));

    assertThat(result.accessToken()).isEqualTo("access.token.value");
    assertThat(result.refreshToken()).isNotBlank();
    assertThat(result.accessTokenExpiresInSeconds()).isEqualTo(900L);
    verify(tokenStore).store(eq(result.refreshToken()), eq(mockUser.getId()), eq(tenantId.toString()), any());
  }

  @Test
  @DisplayName("should throw BusinessRuleViolationException on wrong password")
  void shouldThrowOnWrongPassword() {
    when(userRepository.findByEmailAndTenantId("user@test.com", tenantId))
        .thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("wrong_pass", "hashed_password")).thenReturn(false);

    assertThatThrownBy(() ->
        sut.execute(new LoginUseCase.LoginCommand("user@test.com", "wrong_pass", tenantId.toString())))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("Invalid credentials");
    verifyNoInteractions(tokenStore);
  }

  @Test
  @DisplayName("should throw EntityNotFoundException when user does not exist")
  void shouldThrowWhenUserNotFound() {
    when(userRepository.findByEmailAndTenantId(anyString(), any()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        sut.execute(new LoginUseCase.LoginCommand("missing@test.com", "pw", tenantId.toString())))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("should throw BusinessRuleViolationException when user is inactive")
  void shouldThrowWhenUserInactive() {
    mockUser.deactivate();
    when(userRepository.findByEmailAndTenantId("user@test.com", tenantId))
        .thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);

    assertThatThrownBy(() ->
        sut.execute(new LoginUseCase.LoginCommand("user@test.com", "pw", tenantId.toString())))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("inactive");
  }
}
