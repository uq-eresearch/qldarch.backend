package net.qldarch.security;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;

import net.qldarch.guice.Bind;

@Bind
public class SignIn {

  @Inject
  private UserStore users;

  @Inject
  private SessionStore sessions;

  public SignInResponse signin(String usernameOrEmail, String password) {
    User user = users.getByUsernameOrEmail(usernameOrEmail);
    if(user != null && user.isSignInAllowed() && user.isActivated()) {
      final String encryptedPassword = StringUtils.strip(user.getPassword());
      if(StringUtils.isBlank(encryptedPassword)) {
        return signInOk(user);
      } else {
        final DefaultPasswordService ps = new DefaultPasswordService();
        if(ps.passwordsMatch(StringUtils.strip(password), encryptedPassword)) {
          return signInOk(user);
        }
      }
    }
    return SignInResponse.failed();
  }

  private SignInResponse signInOk(User user) {
    return SignInResponse.ok(user, sessions.newSession(user));
  }

}
