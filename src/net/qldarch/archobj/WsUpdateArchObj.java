package net.qldarch.archobj;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.UpdateEntity;
import net.qldarch.security.User;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Path("/archobj")
public class WsUpdateArchObj {

  @Inject
  private HS hs;

  @Inject @Nullable
  private User user;

  @POST
  @Path("/{id}")
  @Consumes("application/x-www-form-urlencoded")
  @Produces(ContentType.JSON)
  @UpdateEntity(entityClass=ArchObj.class)
  public Response post(@PathParam("id") Long id, MultivaluedMap<String, Object> params) {
    return hs.execute(session -> {
      final ArchObj archobj = hs.get(ArchObj.class, id);
      final Map<String, Object> m = UpdateUtils.asMap(params);
      final String comment = ObjUtils.asString(m.get("comment"));
      if(archobj != null) {
        final Long reqVersion = ObjUtils.asLong(m.get("version"));
        if((reqVersion != null) && !reqVersion.equals(archobj.getVersion()) && (archobj.getVersion() != null)) {
          ArchObjVersion version = hs.get(ArchObjVersion.class, reqVersion);
          if((version != null) && version.getOid().equals(archobj.getId())) {
            archobj.copyFrom(version.getDocumentAsMap());
            archobj.setVersion(version.getId());
            if(archobj.updateFrom(m)) {
              final String revertComment = String.format("reverting to version '%s' before change",
                  version.getId());
              final String newComment = StringUtils.isBlank(comment)?revertComment:
                String.format("%s (%s)", comment, revertComment);
              VersionUtils.createNewVersion(hs, user, archobj, newComment);
              hs.update(archobj);
            } else {
              throw new RuntimeException("no change detected, rollback revert to ealier version");
            }
            return Response.ok().entity(archobj).build();
          } else {
            return Response.status(404).entity(M.of("msg","version not found")).build();
          }
        } else {
          // create an initial version in the version history if it does not exist yet
          if(archobj.getVersion() == null) {
            VersionUtils.createNewVersion(hs, user, archobj, "initial version");
            hs.update(archobj);
          }
          if(archobj.updateFrom(m)) {
            VersionUtils.createNewVersion(hs, user, archobj, comment);
            hs.update(archobj);
          }
        }
        return Response.ok().entity(archobj).build();
      } else {
        return Response.status(404).build();
      }
    });
  }

}
