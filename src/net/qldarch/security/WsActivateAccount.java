package net.qldarch.security;

import java.sql.Timestamp;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;

@Path("/account/activate")
@Slf4j
public class WsActivateAccount {

  @AllArgsConstructor
  @Data
  private static class ActivationResponse {
    private boolean success;
    private Session session;

    public static ActivationResponse failed() {
      return new ActivationResponse(false, null);
    }
  }

  @Inject @Nullable
  private User user;

  @Inject
  private UserStore users;

  @Inject
  private HS hs;

  @Inject
  private SessionStore sessions;

  private Response failed() {
    return Response.ok().entity(ActivationResponse.failed()).build();
  }

  @POST
  @Produces(ContentType.JSON)
  public Response activate(@FormParam("id") Long id, @FormParam("code") String code) {
    final User u = users.get(id);
    if(u == null) {
      log.debug("account with id {} does not exist", id);
      return failed();
    } else if(!u.isSignInAllowed()) {
      log.debug("can not activate account {} as signin is disabled on this account", u.getUsername());
      return failed();
    } else if(u.getActivated() != null) {
      log.debug("account is already activated {}", u.getUsername());
      return failed();
    } else if(!StringUtils.equals(u.getActivation(), code)) {
      log.debug("account activation code wrong {}, supplied code {}", u.getUsername(), code);
      return failed();
    } else {
      u.setActivated(new Timestamp(System.currentTimeMillis()));
      hs.update(u);
      log.info("activation of appuser id {} successful", id);
      if(user == null) {
        final Session session = sessions.newSession(u);
        final ActivationResponse resp = new ActivationResponse(true, session);
        NewCookie cookie = new NewCookie(new NewCookie(
            new Cookie("sessionid",session.getSessionId(), "/", null)));
        return Response.ok(resp).cookie(cookie).build();
      } else {
        return Response.ok(new ActivationResponse(true, null)).build();
      }
    }
  }

}
