package net.qldarch.compobj;

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

@Path("/compobj")
public class WsDeleteCompObj {
  @Inject
  private HS hs;

  @Inject
  private Db db;

  @Inject
  @Nullable
  private User user;

  @DELETE
  @Path("/{id}")
  @SignedIn
  @Produces(ContentType.JSON)
  public Response delete(@PathParam("id") Long id) throws Exception {
    if(user != null) {
      CompObj compobj = hs.get(CompObj.class, id);
      if(compobj != null) {
        if(user.isAdmin() || compobj.getOwner().equals(user.getId())) {
          db.executeVoid(con -> {
            db.execute("delete from timelineevent where compobj = :compobj", M.of("compobj", compobj.getId()));
            db.execute("delete from wordcloud where compobj = :compobj", M.of("compobj", compobj.getId()));
            db.execute("delete from compobjstructure where compobj = :compobj", M.of("compobj", compobj.getId()));
            db.execute("delete from compobj where id = :id", M.of("id", compobj.getId()));
          });
          return Response.ok().entity(M.of("id", compobj.getId())).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Compound object not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
