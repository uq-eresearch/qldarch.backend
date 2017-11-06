package net.qldarch.archobj;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import net.qldarch.gson.JsonSkipField;
import net.qldarch.gson.serialize.CollectionRemoveNullsSerializer;
import net.qldarch.gson.serialize.JsonSerializer;
import net.qldarch.hibernate.HS;
import net.qldarch.interview.InterviewUtteranceSerializer;
import net.qldarch.interview.Utterance;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.media.Media;
import net.qldarch.search.Index;
import net.qldarch.search.update.UpdateArchObjJob;
import net.qldarch.security.UpdateEntity;
import net.qldarch.security.User;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

@Path("/archobj")
public class WsCreateArchObj {

  @Inject
  private HS hs;

  @Inject @Nullable
  private User user;

  @Inject
  private Index index;

  @PUT
  @Consumes("application/x-www-form-urlencoded")
  @Produces(ContentType.JSON)
  @JsonSkipField(type=Media.class, field="depicts")
  @JsonSerializer(type=Utterance.class, serializer=InterviewUtteranceSerializer.class)
  @JsonSerializer(path="$.precededby", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.succeededby", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.interviews", serializer=CollectionRemoveNullsSerializer.class)
  @JsonSerializer(path="$.interviews.*", serializer=IdArchObjSerializer.class)
  @JsonSerializer(path="$.interviewer.*", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.interviewee.*", serializer=SimpleArchObjSerializer.class)
  public Response create(MultivaluedMap<String, Object> params) {
    if((user != null) && (user.isAdmin() || user.isEditor())) {
      final Map<String, Object> m = UpdateUtils.asMap(params);
      final ArchObjType type = ArchObjType.of(ObjUtils.asString(m.get("type")));
      if(type == null) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown type")).build();
      }
      final String label = ObjUtils.asString(m.get("label"));
      if(StringUtils.isBlank(label)) {
        return Response.status(400).entity(M.of("msg", "Missing label")).build();
      }
      try {
        ArchObj object = type.getImplementingClass().newInstance();
        object.setType(type);
        object.copyFrom(m);
        object.setOwner(user.getId());
        object.setCreated(new Date(Instant.now().toEpochMilli()));
        hs.save(object);
        object.postCreate(m);
        VersionUtils.createNewVersion(hs, user, object, "initial version");
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try(Directory directory = index.directory()) {
          try(IndexWriter writer = new IndexWriter(directory, config)) {
            new UpdateArchObjJob(object).run(writer);
            writer.commit();
          } catch(Exception e) {
            throw new RuntimeException("update search index failed", e);
          }
        } catch(IOException e) {
          throw new RuntimeException("failed to open search directory", e);
        }
        return Response.ok().entity(object).build();
      } catch(Exception e) {
        throw new RuntimeException("failed to create archive object", e);
      }
    } else {
      return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
    }
  }

}
