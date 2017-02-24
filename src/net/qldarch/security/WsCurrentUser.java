package net.qldarch.security;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;

@Path("/user")
public class WsCurrentUser {

  @Inject @Nullable
  private User user;

  @GET
  @Produces(ContentType.JSON)
  public User currentUser() {
    return user;
  }
}
