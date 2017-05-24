package net.qldarch.relationship;

import java.sql.Timestamp;
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

import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.User;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Path("/interviewrelationship")
public class WsCreateInterviewRelationship {

  @Inject
  private HS hs;

  @Inject
  @Nullable
  private User user;

  @PUT
  @Consumes("application/x-www-form-urlencoded")
  @Produces(ContentType.JSON)
  public Response create(MultivaluedMap<String, Object> params) {
    if((user != null) && (user.isAdmin() || user.isEditor())) {
      final Map<String, Object> m = UpdateUtils.asMap(params);
      final RelationshipType type = RelationshipType.valueOf(ObjUtils.asString(m.get("type")));
      final RelationshipSource source = RelationshipSource.valueOf(ObjUtils.asString(m.get("source")));
      final long subject = ObjUtils.asLong(m.get("subject"));
      final long object = ObjUtils.asLong(m.get("object"));
      final long interview = ObjUtils.asLong(m.get("interview"));
      final long utterance = ObjUtils.asLong(m.get("utterance"));
      if(type == null) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown type")).build();
      }
      if(source == null) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown source")).build();
      }
      if(source != RelationshipSource.interview) {
        return Response.status(400).entity(M.of("msg", "Non-interview source")).build();
      }
      if(subject == 0L) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown subject")).build();
      }
      if(object == 0L) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown object")).build();
      }
      if(interview == 0L) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown interview")).build();
      }
      if(utterance == 0L) {
        return Response.status(400).entity(M.of("msg", "Missing or unknown utterance")).build();
      }
      try {
        InterviewRelationship interviewrelationship = new InterviewRelationship();
        interviewrelationship.setType(type);
        interviewrelationship.setSource(source);
        interviewrelationship.setSubject(subject);
        interviewrelationship.setObject(object);
        interviewrelationship.setInterview(interview);
        interviewrelationship.setUtterance(utterance);
        interviewrelationship.setOwner(user.getId());
        if(m.containsKey("note")) {
          interviewrelationship.setNote(ObjUtils.asString(m.get("note")));
        }
        if(m.containsKey("from")) {
          interviewrelationship.setFrom(ObjUtils.asInteger(m.get("from")));
        }
        if(m.containsKey("until")) {
          interviewrelationship.setUntil(ObjUtils.asInteger(m.get("until")));
        }
        interviewrelationship.setCreated(new Timestamp(Instant.now().toEpochMilli()));
        hs.save(interviewrelationship);
        return Response.ok().entity(interviewrelationship).build();
      } catch(Exception e) {
        throw new RuntimeException("failed to create interview relationship object", e);
      }
    } else {
      return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
    }
  }

}
