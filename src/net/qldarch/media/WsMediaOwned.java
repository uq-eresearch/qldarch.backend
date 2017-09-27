package net.qldarch.media;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.User;

@Path("media/owned")
@Slf4j
public class WsMediaOwned {

  @Inject
  @Nullable
  private User user;

  @Inject
  private Db db;

  @GET
  @Produces(ContentType.JSON)
  public List<Map<String, Object>> get() {
    try {
      if(user != null) {
        return db.executeQuery(
            "with o as (select id as oid, label as depictslabel, type as depictstype from archobj)"
                + " select id, title as label, creator, created, filename, filesize,"
                + " mimetype, identifier, rights, type, owner, depicts, depictslabel, depictstype"
                + " from media join o on media.depicts = o.oid where owner = " + user.getId()
                + " and depicts is not null and deleted is null", Rsc::fetchAll);
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
