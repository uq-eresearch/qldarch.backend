package net.qldarch.security;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Path("/account/update")
@Slf4j
public class WsUpdateUser {

  @Inject
  @Nullable
  private User user;

  @Inject
  private UserStore users;

  @Inject
  private HS hs;

  @POST
  @Path("/{id}")
  @Produces(ContentType.JSON)
  public Response update(@PathParam("id") Long id, MultivaluedMap<String, Object> params) {
    final User u = users.get(id);
    if(u != null) {
      if(user != null && user.isAdmin()) {
        final Map<String, Object> m = UpdateUtils.asMap(params);
        if(m.containsKey("contact")) {
          u.setContact(ObjUtils.asBoolean(m.get("contact")));
        }
        if(m.containsKey("displayName")) {
          u.setDisplayName(ObjUtils.asString(m.get("displayName")));
        }
        if(m.containsKey("email")) {
          u.setEmail(ObjUtils.asString(m.get("email")));
        }
        if(m.containsKey("role")) {
          u.setRole(ObjUtils.asString(m.get("role")));
        }
        if(m.containsKey("username")) {
          u.setUsername(ObjUtils.asString(m.get("username")));
        }
        hs.update(u);
        log.info("updating of appuser id {} successful", id);
        return Response.ok().entity(u).build();
      } else {
        return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
      }
    } else {
      log.debug("account with id {} does not exist", id);
      return Response.status(404).entity(M.of("msg", "User account not found")).build();
    }
  }
}
