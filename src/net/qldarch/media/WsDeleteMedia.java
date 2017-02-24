package net.qldarch.media;

import java.sql.Timestamp;
import java.time.Instant;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import net.qldarch.hibernate.HS;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;

@Path("media")
public class WsDeleteMedia {

  @Inject @Nullable
  private User user;

  @Inject
  private HS hs;

  @DELETE
  @Path("/{id}")
  @SignedIn
  public Response delete(@PathParam("id") Long id) {
    if(user != null) {
      Media media = hs.get(Media.class, id);
      if(media != null) {
        if(user.isAdmin() || user.getId().equals(media.getOwner())) {
          media.setDeleted(new Timestamp(Instant.now().toEpochMilli()));
          hs.update(media);
          return Response.ok().build();
        }
      } else {
        Response.status(404).build();
      }
    }
    return Response.status(403).build();
  }

}
