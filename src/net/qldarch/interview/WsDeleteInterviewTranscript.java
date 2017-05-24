package net.qldarch.interview;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.archobj.ArchObj;
import net.qldarch.db.Db;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.media.Media;
import net.qldarch.relationship.InterviewRelationship;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;

@Path("interview/transcript")
public class WsDeleteInterviewTranscript {

  @Inject
  private HS hs;

  @Inject
  private Db db;

  @Inject
  @Nullable
  private User user;

  @DELETE
  @Path("/{id}")
  @SignedIn
  @Produces(ContentType.JSON)
  public Response delete(@PathParam("id") Long id) throws Exception {
    if((user != null) && (user.isAdmin())) {
      Interview interview = hs.get(Interview.class, id);
      if(interview != null) {
        List<InterviewRelationship> r = hs.execute(
            session -> session.createQuery("from InterviewRelationship where interview = :interview",
                InterviewRelationship.class).setParameter("interview", interview.getId())).getResultList();
        if(r.isEmpty()) {
          List<Media> media = hs.execute(
              session -> session.createQuery("from Media where type = 'Transcript' and depicts = :depicts",
                  Media.class).setParameter("depicts", (ArchObj) interview)).getResultList();
          if(!media.isEmpty()) {
            db.executeVoid(con -> {
              db.execute("delete from utterance where interview = :id", M.of("id", interview.getId()));
            });
            for(Media m : media) {
              m.setDeleted(new Timestamp(Instant.now().toEpochMilli()));
              hs.update(m);
            }
            return Response.ok().entity(M.of("id", interview.getId(), "label", interview.getLabel())).build();
          } else {
            return Response.status(404).entity(M.of("msg", "Transcript file not found")).build();
          }
        } else {
          return Response.status(409).entity(M.of("msg", "Relationship detected")).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Missing or unknown interview")).build();
      }
    } else {
      return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
    }
  }
}
