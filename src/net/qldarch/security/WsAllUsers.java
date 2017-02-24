package net.qldarch.security;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;

@Path("/accounts/all")
public class WsAllUsers {

  @Inject
  private UserStore users;

  @GET
  @Produces(ContentType.JSON)
  @Admin
  public List<User> all() {
    return users.all();
  }

}
