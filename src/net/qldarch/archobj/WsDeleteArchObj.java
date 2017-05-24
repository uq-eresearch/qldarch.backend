package net.qldarch.archobj;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.hibernate.HS;
import net.qldarch.interview.Interview;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.person.Person;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;

@Path("/archobj")
public class WsDeleteArchObj {

  @Inject
  private HS hs;

  @Inject @Nullable
  private User user;

  @DELETE
  @Path("/{id}")
  @SignedIn
  @Produces(ContentType.JSON)
  public Response delete(@PathParam("id") Long id) {
    if(user != null) {
      ArchObj o = hs.get(ArchObj.class, id);
      if(o != null) {
        if(user.isAdmin() || o.getOwner().equals(user.getId())) {
          Timestamp deleted = new Timestamp(Instant.now().toEpochMilli());
          if(o instanceof Person) {
            final Person person = (Person) o;
            for(Interview interview : person.getInterviews()) {
              interview.setDeleted(deleted);
              hs.update(interview);
            }
          }
          o.setDeleted(deleted);
          hs.update(o);
          return Response.ok().entity(M.of("id", o.getId(), "label", o.getLabel())).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Archive object not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }
}
