package net.qldarch.media;

import java.awt.Dimension;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;
import net.qldarch.security.Admin;

@Path("media/thumbnails")
@Slf4j
public class WsPrepareThumbnails {

  @Inject
  private Thumbnails thumbnails;

  @Inject
  private HS hs;

  private void run(Dimension d) {
    log.info("creating thumbnails with dimension {}", d);
    hs.executeVoid(session -> {
      try {
        session.createQuery("from Media", Media.class).getResultList().forEach(
            media -> thumbnails.get(media, d));
        log.info("finished creating thumbnails with dimension {}", d);
      } catch(Exception e) {
        log.warn("failed to create thumbnail with dimension {}", d, e);
      }
    });
  }

  @POST
  @Admin
  public void prepare(@QueryParam("dimension") Dimension d) {
    new Thread(() -> run(d)).start();
  }

}
