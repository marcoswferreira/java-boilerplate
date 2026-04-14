package com.boilerplate.web.controller;

import com.boilerplate.core.port.in.auth.LoginUseCase;
import com.boilerplate.core.port.in.auth.LogoutUseCase;
import com.boilerplate.core.port.in.auth.RefreshTokenUseCase;
import com.boilerplate.web.dto.request.LoginRequest;
import com.boilerplate.web.dto.request.LogoutRequest;
import com.boilerplate.web.dto.request.RefreshRequest;
import com.boilerplate.web.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for authentication operations. */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;

  public AuthController(
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase) {
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
  }

  @PostMapping("/login")
  @SecurityRequirements({}) // Public endpoint — no JWT required
  @Operation(summary = "Login", description = "Authenticates user credentials and returns a JWT token pair")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    var result = loginUseCase.execute(
        new LoginUseCase.LoginCommand(request.email(), request.password(), request.tenantId()));
    return ResponseEntity.ok(
        TokenResponse.of(result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
  }

  @PostMapping("/refresh")
  @SecurityRequirements({}) // Public endpoint — refresh token in body serves as credential
  @Operation(summary = "Refresh access token", description = "Issues a new access token using a valid refresh token")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    var result = refreshTokenUseCase.execute(
        new RefreshTokenUseCase.RefreshCommand(request.refreshToken()));
    return ResponseEntity.ok(
        TokenResponse.of(result.accessToken(), result.refreshToken(), result.accessTokenExpiresInSeconds()));
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout", description = "Revokes the provided refresh token")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
    logoutUseCase.execute(new LogoutUseCase.LogoutCommand(request.refreshToken()));
    return ResponseEntity.noContent().build();
  }
}
