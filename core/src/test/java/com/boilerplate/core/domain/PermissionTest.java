package com.boilerplate.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Permission value object")
class PermissionTest {

  @Test
  @DisplayName("should parse RESOURCE:ACTION format")
  void shouldParsePermission() {
    Permission p = Permission.parse("bet:place");
    assertThat(p.getResource()).isEqualTo("bet");
    assertThat(p.getAction()).isEqualTo("place");
    assertThat(p.toPermissionString()).isEqualTo("bet:place");
  }

  @Test
  @DisplayName("should be equal when resource and action match")
  void shouldBeEqual() {
    Permission a = Permission.of("user", "read");
    Permission b = Permission.of("user", "read");
    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  @DisplayName("should throw on invalid format")
  void shouldThrowOnInvalidFormat() {
    assertThatThrownBy(() -> Permission.parse("invalid-no-colon"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RESOURCE:ACTION");
  }

  @Test
  @DisplayName("should normalize to lowercase")
  void shouldNormalizeToLowercase() {
    Permission p = Permission.of("BET", "PLACE");
    assertThat(p.toPermissionString()).isEqualTo("bet:place");
  }
}
