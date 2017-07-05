package net.qldarch.media;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.search.Index;
import net.qldarch.search.update.UpdateMediaJob;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.DateUtil;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

@Path("media")
public class WsUpdateMedia {

  @Inject
  @Nullable
  private User user;

  @Inject
  private HS hs;

  @Inject
  private MediaArchive archive;

  @Inject
  private Index index;

  @POST
  @Path("/{id}")
  @SignedIn
  @Produces(ContentType.JSON)
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
          Analyzer analyzer = new StandardAnalyzer();
          IndexWriterConfig config = new IndexWriterConfig(analyzer);
          try(Directory directory = index.directory()) {
            try(IndexWriter writer = new IndexWriter(directory, config)) {
              new UpdateMediaJob(media, archive).run(writer);
              writer.commit();
            } catch(Exception e) {
              throw new RuntimeException("update search index failed", e);
            }
          } catch(IOException e) {
            throw new RuntimeException("failed to open search directory", e);
          }
          return Response.ok().entity(media).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Media not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
