package net.qldarch.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
class AuthResult {
  @Getter
  boolean allow;
  @Getter
  String reason;

  public static AuthResult allow() {
    return new AuthResult(true, null);
  }

  public static AuthResult deny(String reason) {
    return new AuthResult(false, reason);
  }
}
