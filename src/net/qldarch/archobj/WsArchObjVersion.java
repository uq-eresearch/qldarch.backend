package net.qldarch.archobj;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.User;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Slf4j
@Path("archobjversion")
public class WsArchObjVersion {

  @Inject
  @Nullable
  private User user;

  @Inject
  private Db db;

  @POST
  @Produces(ContentType.JSON)
  public List<Map<String, Object>> get(MultivaluedMap<String, Object> params) {
    try {
      if(user != null && user.isAdmin()) {
        final Map<String, Object> m = UpdateUtils.asMap(params);
        return db.executeQuery(
            "select * from archobjversion where created >= '" + ObjUtils.asString(m.get("startdate"))
                + " 00:00' and created <= '" + ObjUtils.asString(m.get("enddate")) + " 24:00'", Rsc::fetchAll);
      } else {
        log.debug("user {} is not admin", user);
      }
    } catch(Exception e) {
      log.debug("retrieving archobj version failed for user {} ", user, e);
    }
    return null;
  }
}
