package net.qldarch.relationship;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.gson.serialize.Json;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;
import net.qldarch.util.RandomString;

import com.google.gson.Gson;

@Path("/relationship/{id}")
public class WsDeleteRelationship {

  @Inject
  private HS hs;

  @Inject
  private Db db;

  @Inject
  @Nullable
  private User user;

  @Inject
  private RandomString rstr;

  @DELETE
  @SignedIn
  @Produces(ContentType.JSON)
  public Response delete(@PathParam("id") Long id) throws Exception {
    if(user != null) {
      Relationship r = hs.get(Relationship.class, id);
      if(r != null) {
        if(user.isAdmin() || r.getOwner().equals(user.getId())) {
          db.executeVoid(con -> {
            final long now = System.currentTimeMillis();
            Timestamp created = new Timestamp(now);
            String trx = String.format("%012x_%s", now, rstr.next());
            List<Map<String, Object>> relationships = db.executeQuery(
                "select * from relationship where id = " + r.getId(), Rsc::fetchAll);
            for(Map<String, Object> relationship : relationships) {
              db.execute(
                  "insert into relationshiplog(trxid, owner, created,"
                      + " relationship, action, field, sourcedocument) values (:trxid, :owner, :created,"
                      + " :relationship, :action, :field, :sourcedocument)",
                  M.of("trxid", trx, "owner", user.getId(), "created", created, "relationship",
                      relationship.get("id"), "action", "delete", "field", "all", "sourcedocument",
                      new Gson().toJson(new Json().toJsonTree(relationship))));
            }
            db.execute("delete from interviewrelationship where id = :id", M.of("id", r.getId()));
            db.execute("delete from relationship where id = :id", M.of("id", r.getId()));
          });
          return Response.ok().entity(M.of("id", r.getId())).build();
        }
      } else {
        return Response.status(404).entity(M.of("msg", "Relationship not found")).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
