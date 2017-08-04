package net.qldarch.relationship;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.gson.serialize.Json;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.SignedIn;
import net.qldarch.security.User;
import net.qldarch.util.M;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.RandomString;
import net.qldarch.util.UpdateUtils;

import com.google.gson.Gson;

@Path("relationships/move")
public class WsMoveRelationships {

  @Inject
  private Db db;

  @Inject
  @Nullable
  private User user;

  @Inject
  private RandomString rstr;

  @POST
  @SignedIn
  @Produces(ContentType.JSON)
  public Response move(MultivaluedMap<String, Object> params) throws Exception {
    if(user != null) {
      if(user.isAdmin()) {
        final Map<String, Object> m = UpdateUtils.asMap(params);
        Long from = ObjUtils.asLong(m.get("from"));
        Long to = ObjUtils.asLong(m.get("to"));
        db.executeVoid(con -> {
          final long now = System.currentTimeMillis();
          Timestamp created = new Timestamp(now);
          String trx = String.format("%012x_%s", now, rstr.next());
          List<Map<String, Object>> bySubject = db.executeQuery("select * from relationship where subject = "
              + from, Rsc::fetchAll);
          for(Map<String, Object> relationship : bySubject) {
            db.execute("insert into relationshiplog(trxid, owner, created, relationship, action, field,"
                + " oidfrom, oidto, sourcedocument)"
                + " values (:trxid, :owner, :created, :relationship, :action, :field,"
                + " :oidfrom, :oidto, :sourcedocument)", M.of("trxid", trx, "owner", user.getId(), "created",
                created, "relationship", relationship.get("id"), "action", "move", "field", "subject", "oidfrom",
                from, "oidto", to, "sourcedocument", new Gson().toJson(new Json().toJsonTree(relationship))));
          }
          db.execute("update relationship set subject = :to where subject = :from", M.of("to", to, "from", from));
          List<Map<String, Object>> byObject = db.executeQuery("select * from relationship where object = " + from,
              Rsc::fetchAll);
          for(Map<String, Object> relationship : byObject) {
            db.execute("insert into relationshiplog(trxid, owner, created, relationship, action, field,"
                + " oidfrom, oidto, sourcedocument)"
                + " values (:trxid, :owner, :created, :relationship, :action, :field,"
                + " :oidfrom, :oidto, :sourcedocument)", M.of("trxid", trx, "owner", user.getId(), "created",
                created, "relationship", relationship.get("id"), "action", "move", "field", "object", "oidfrom",
                from, "oidto", to, "sourcedocument", new Gson().toJson(new Json().toJsonTree(relationship))));
          }
          db.execute("update relationship set object = :to where object = :from", M.of("to", to, "from", from));
        });
        return Response.ok().entity(M.of("from", from, "to", to)).build();
      }
    }
    return Response.status(403).entity(M.of("msg", "Unauthorised user")).build();
  }

}
