package net.qldarch.relationship;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.db.Db;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;

@Path("/relationship/{id}")
public class WsDeleteRelationship {

  @Inject
  private HS hs;

  @Inject
  private Db db;

  @Inject @Nullable
  private User user;

  @DELETE
  @SignedIn
  @Produces(ContentType.JSON)
  public Response delete(@PathParam("id") Long id) throws Exception {
    if(user != null) {
      Relationship r = hs.get(Relationship.class, id);
      if(r != null) {
        if(user.isAdmin() || r.getOwner().equals(user.getId())) {
          db.executeVoid(con -> {
            db.execute("delete from interviewrelationship where id = :id", M.of("id", r.getId()));
            db.execute("delete from relationship where id = :id", M.of("id", r.getId()));
          });
          return Response.ok().entity(M.of("id", r.getId())).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Relationship not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
