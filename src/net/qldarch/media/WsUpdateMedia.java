package net.qldarch.media;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.qldarch.hibernate.HS;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.DateUtil;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Path("media")
public class WsUpdateMedia {

  @Inject
  @Nullable
  private User user;

  @Inject
  private HS hs;

  @POST
  @Path("/{id}")
  @SignedIn
  public Response post(@PathParam("id") Long id, MultivaluedMap<String, Object> params) {
    if(user != null) {
      Media media = hs.get(Media.class, id);
      if(media != null) {
        if(user.isAdmin() || user.getId().equals(media.getOwner())) {
          final Map<String, Object> m = UpdateUtils.asMap(params);
          if(m.containsKey("label")) {
            media.setLabel(ObjUtils.asString(m.get("label")));
          }
          if(m.containsKey("description")) {
            media.setDescription(ObjUtils.asString(m.get("description")));
          }
          if(m.containsKey("type")) {
            media.setType(MediaType.valueOf(ObjUtils.asString(m.get("type"))));
          }
          if(m.containsKey("creator")) {
            media.setCreator(ObjUtils.asString(m.get("creator")));
          }
          if(m.containsKey("created")) {
            media.setCreated(DateUtil.toSqlDate(ObjUtils.asDate(m.get("created"), "yyyy-MM-dd")));
          }
          if(m.containsKey("rights")) {
            media.setRights(ObjUtils.asString(m.get("rights")));
          }
          if(m.containsKey("identifier")) {
            media.setIdentifier(ObjUtils.asString(m.get("identifier")));
          }
          if(m.containsKey("location")) {
            media.setLocation(ObjUtils.asString(m.get("location")));
          }
          if(m.containsKey("projectnumber")) {
            media.setProjectnumber(ObjUtils.asString(m.get("projectnumber")));
          }
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
