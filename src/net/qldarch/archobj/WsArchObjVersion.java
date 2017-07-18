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
        if(m.containsKey("oid")) {
          return fromOid(ObjUtils.asLong(m.get("oid")));
        }
        if(m.containsKey("startdate") && m.containsKey("enddate")) {
          return fromDates(ObjUtils.asString(m.get("startdate")), ObjUtils.asString(m.get("enddate")));
        }
      } else {
        log.debug("user {} is not admin", user);
      }
    } catch(Exception e) {
      log.debug("retrieving archobj version failed for user {} ", user, e);
    }
    return null;
  }

  private List<Map<String, Object>> fromDates(String startDate, String endDate) {
    try {
      return db.executeQuery("select * from archobjversion where created >= '" + startDate
          + " 00:00' and created <= '" + endDate + " 24:00'", Rsc::fetchAll);
    } catch(Exception e) {
      log.debug("retrieving archobj version by created dates failed for user {} ", user, e);
    }
    return null;
  }

  private List<Map<String, Object>> fromOid(Long oId) {
    try {
      return db.executeQuery("select * from archobjversion where oid = " + oId, Rsc::fetchAll);
    } catch(Exception e) {
      log.debug("retrieving archobj version by oid failed for user {} ", user, e);
    }
    return null;
  }

}
