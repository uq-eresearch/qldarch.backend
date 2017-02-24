package net.qldarch.security;

import javax.annotation.Nullable;
import javax.inject.Inject;

import net.qldarch.guice.Bind;

@Bind
public class SignOut {

  @Inject @Nullable
  private Session session;

  @Inject
  private SessionStore sessions;

  public void signout() {
    sessions.expire(session);
  }

}
