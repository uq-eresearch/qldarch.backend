package net.qldarch.compobj;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.archobj.IdArchObjSerializer;
import net.qldarch.archobj.SimpleArchObjSerializer;
import net.qldarch.gson.JsonSkipField;
import net.qldarch.gson.serialize.CollectionRemoveNullsSerializer;
import net.qldarch.gson.serialize.JsonSerializer;
import net.qldarch.hibernate.HS;
import net.qldarch.interview.InterviewUtteranceSerializer;
import net.qldarch.interview.Utterance;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.media.Media;

@Path("/compobj")
public class WsCompObj {

  @Inject
  private HS hs;

  @GET
  @Path("/{id}")
  @Produces(ContentType.JSON)
  @JsonSkipField(type = Media.class, field = "depicts")
  @JsonSerializer(type = Utterance.class, serializer = InterviewUtteranceSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.precededby", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.succeededby", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviews", serializer = CollectionRemoveNullsSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviews.*", serializer = IdArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviewer.*", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviewee.*", serializer = SimpleArchObjSerializer.class)
  public Response get(@PathParam("id") Long id) {
    CompObj compobj = hs.get(CompObj.class, id);
    return Response.ok().entity(compobj).build();
  }
}
