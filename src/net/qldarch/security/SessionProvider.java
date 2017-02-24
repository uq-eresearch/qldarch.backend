package net.qldarch.security;

import java.util.Arrays;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Provider;

import net.qldarch.guice.Bind;

@Bind(provides=Session.class)
public class SessionProvider implements Provider<Session> {

  @Inject
  private HttpServletRequest request;

  private Cookie[] cookies() {
    final Cookie[] cookies = request.getCookies();
    return cookies != null?cookies:new Cookie[0];
  }

  private Session session(Cookie cookie) {
    return ((cookie != null) && (cookie.getValue() != null))?new Session(cookie.getValue()):null;
  }

  @Override
  public Session get() {
    return session(Arrays.stream(cookies()).filter(cookie -> StringUtils.equalsIgnoreCase(
        "sessionid", cookie.getName())).findFirst().orElse(null));
  }

}
