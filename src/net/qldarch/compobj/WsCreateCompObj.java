package net.qldarch.compobj;

import java.sql.Date;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
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
import net.qldarch.security.User;
import net.qldarch.structure.Structure;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

@Path("/compobj")
public class WsCreateCompObj {

  @Inject
  private HS hs;

  @Inject
  @Nullable
  private User user;

  @PUT
  @Consumes("application/x-www-form-urlencoded")
  @Produces(ContentType.JSON)
  @JsonSkipField(type = Media.class, field = "depicts")
  @JsonSerializer(type = Utterance.class, serializer = InterviewUtteranceSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.precededby", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.succeededby", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviews", serializer = CollectionRemoveNullsSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviews.*", serializer = IdArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviewer.*", serializer = SimpleArchObjSerializer.class)
  @JsonSerializer(path = "$.timelineevent[*].archobj.interviewee.*", serializer = SimpleArchObjSerializer.class)
  public Response create(MultivaluedMap<String, Object> params) {
    if((user != null) && (user.isAdmin() || user.isEditor())) {
      final Map<String, Object> m = UpdateUtils.asMap(params);
      final CompObjType type = CompObjType.of(ObjUtils.asString(m.get("type")));
      if(type == null) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown type")).build();
      }
      final String title = ObjUtils.asString(m.get("title"));
      if(StringUtils.isBlank(title)) {
        return Response.status(400).entity(M.of("msg", "Missing title")).build();
      }
      try {
        CompObj object = new CompObj();
        object.setTitle(title);
        object.setType(type);
        object.setOwner(user.getId());
        object.setModified(new Date(Instant.now().toEpochMilli()));
        if(type == CompObjType.map) {
          Set<Long> structureId = ObjUtils.asLongSet(m.get("structure"));
          if(structureId != null) {
            Set<Structure> structures = new HashSet<>();
            for(Long s : structureId) {
              Structure structure = new Structure();
              structure.setId(s);
              structures.add(structure);
            }
            object.setStructure(structures);
          }
        }
        hs.save(object);
        if(type == CompObjType.timeline) {
          final Set<String> timelineevents = ObjUtils.asStringSet(m.get("timelineevent"));
          if(timelineevents != null) {
            for(String t : timelineevents) {
              ObjectMapper mapper = new ObjectMapper();
              mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
              TimelineEvent timelineevent = mapper.readValue(t, TimelineEvent.class);
              timelineevent.setCompobj(object.getId());
              hs.save(timelineevent);
            }
          }
        }
        if(type == CompObjType.wordcloud) {
          final Set<String> wordclouds = ObjUtils.asStringSet(m.get("wordcloud"));
          if(wordclouds != null) {
            for(String w : wordclouds) {
              ObjectMapper mapper = new ObjectMapper();
              mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
              WordCloud wordcloud = mapper.readValue(w, WordCloud.class);
              wordcloud.setCompobj(object.getId());
              hs.save(wordcloud);
            }
          }
        }
        return Response.ok().entity(object).build();
      } catch(Exception e) {
        throw new RuntimeException("failed to create compound object", e);
      }
    } else {
      return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
    }
  }
}
