package net.qldarch.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SignUpResponse {

  @Getter
  private final boolean success;

  @Getter
  private final String reason;

  static SignUpResponse failed(String reason) {
    return new SignUpResponse(false, reason);
  }

  static SignUpResponse ok() {
    return new SignUpResponse(true, null);
  }

}
