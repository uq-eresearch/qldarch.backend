package net.qldarch.media;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.User;

@Path("media/owned")
@Slf4j
public class WsMediaOwned {

  @Inject
  @Nullable
  private User user;

  @Inject
  private HS hs;

  @GET
  @Produces(ContentType.JSON)
  public List<Media> get() {
    try {
      if(user != null) {
        return hs.execute(session -> session
            .createQuery("from Media m where m.owner = :owner and m.depicts is not null and m.deleted is null",
                Media.class).setParameter("owner", user.getId()).getResultList());
      } else {
        log.debug("media by owner failed as user is {}", user);
        return null;
      }
    } catch(Exception e) {
      log.debug("media by owner failed for user {} ", user, e);
    }
    return null;
  }

}
