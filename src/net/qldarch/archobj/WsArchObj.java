package net.qldarch.archobj;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.db.Sql;
import net.qldarch.gson.JsonSkipField;
import net.qldarch.gson.serialize.CollectionRemoveNullsSerializer;
import net.qldarch.gson.serialize.JsonSerializer;
import net.qldarch.hibernate.HS;
import net.qldarch.interview.Interview;
import net.qldarch.interview.InterviewUtteranceSerializer;
import net.qldarch.interview.Utterance;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.media.Media;
import net.qldarch.relationship.TranscriptRelationshipSetup;
import net.qldarch.util.M;

@Path("/archobj")
public class WsArchObj {

  @Inject
  private HS hs;

  @Inject
  private Db db;

  @Inject
  private TranscriptRelationshipSetup transcriptSetup;

  @GET
  @Path("/{id}")
  @Produces(ContentType.JSON)
  @JsonSkipField(type=Media.class, field="depicts")
  @JsonSerializer(type=Utterance.class, serializer=InterviewUtteranceSerializer.class)
  @JsonSerializer(path="$.precededby", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.succeededby", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.interviews", serializer=CollectionRemoveNullsSerializer.class)
  @JsonSerializer(path="$.interviews.*", serializer=IdArchObjSerializer.class)
  @JsonSerializer(path="$.interviewer.*", serializer=SimpleArchObjSerializer.class)
  @JsonSerializer(path="$.interviewee.*", serializer=SimpleArchObjSerializer.class)
  public Response get(@PathParam("id") Long id) throws Exception {
    final ArchObj archobj = hs.get(ArchObj.class, id);
    if(archobj != null) {
      if(archobj.isDeleted()) {
        return Response.status(404).build();
      }
      archobj.setRelationships(
          db.executeQuery(new Sql(this).prepare(), M.of("id", archobj.getId()), Rsc::fetchAll));
      archobj.setAssociatedMedia(
          db.executeQuery("select am.media, archobj.label, archobj.id  as depicts,"
              + " media.type, media.mimetype"
              + " from associatedmedia am join media on am.media = media.id"
              + " join archobj on media.depicts = archobj.id where am.associated = :id"
              + " and archobj.deleted is null and media.deleted is null",
              M.of("id", archobj.getId()), Rsc::fetchAll));
      if(archobj instanceof Interview) {
        transcriptSetup.setup((Interview)archobj);
      }
    }
    return Response.ok().entity(archobj).build();
  }
}
