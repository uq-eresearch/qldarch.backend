package net.qldarch.security;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.jaxrs.ContentType;

@Path("/signout")
@Slf4j
public class WsSignOut {

  @Inject
  private SignOut signout;

  @Inject @Nullable
  private User user;

  @Inject @Nullable
  private Session session;

  @POST
  @Produces(ContentType.JSON)
  public Response signout() {
    log.info("signout session '{}', user '{}'", (session!=null?session.getSessionId(): "(null)"),
        (user!=null?user.getUsername():"(null)"));
    signout.signout();
    NewCookie cookie = new NewCookie(new NewCookie(
        new Cookie("sessionid","", "/", null)));
    return Response.ok("{}").cookie(cookie).build();
  }
}
