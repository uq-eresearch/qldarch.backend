package net.qldarch.security;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.jaxrs.ContentType;

@Path("/signin")
@Slf4j
public class WsSignIn {

  @Inject
  private SignIn signin;

  @POST
  @Produces(ContentType.JSON)
  public Response signin(@FormParam("email") String usernameOrEmail,
      @FormParam("password") String password) {
    final SignInResponse s = signin.signin(usernameOrEmail, password);
    if(s.isSuccess()) {
      log.info("signin ok for '{}', session '{}'", usernameOrEmail, s.getSession().getSessionId());
      NewCookie cookie = new NewCookie(new NewCookie(
          new Cookie("sessionid",s.getSession().getSessionId(), "/", null)));
      return Response.ok(s).cookie(cookie).build();
    } else {
      log.info("signin failed for '{}'", usernameOrEmail);
      return Response.ok(s).build();
    }
  }

}
