package net.qldarch.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SignInResponse {

  @Getter
  private final boolean success;

  @Getter
  private final User user;

  @Getter
  private final Session session;

  public static SignInResponse failed() {
    return new SignInResponse(false, null, null);
  }

  public static SignInResponse ok(User user, Session session) {
    return new SignInResponse(true, user, session);
  }

}
