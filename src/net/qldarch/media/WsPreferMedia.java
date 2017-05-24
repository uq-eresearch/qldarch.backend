package net.qldarch.media;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;

@Path("media/prefer")
public class WsPreferMedia {

  @Inject
  @Nullable
  private User user;

  @Inject
  private HS hs;

  @POST
  @Path("/{id}")
  @SignedIn
  @Produces(ContentType.JSON)
  public Response post(@PathParam("id") Long id) {
    if(user != null) {
      Media media = hs.get(Media.class, id);
      if(media != null) {
        if(user.isAdmin() || user.getId().equals(media.getOwner())) {
          media.setPreferred(new Timestamp(Instant.now().toEpochMilli()));
          hs.update(media);
          return Response.ok().entity(M.of("id", media.getId(), "filename", media.getFilename())).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Media not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
