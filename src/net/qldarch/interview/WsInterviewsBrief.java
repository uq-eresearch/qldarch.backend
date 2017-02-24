package net.qldarch.interview;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.db.Sql;
import net.qldarch.jaxrs.ContentType;

@Path("/interviews/brief")
public class WsInterviewsBrief {

  @Inject
  private Db db;

  @GET
  @Produces(ContentType.JSON)
  public List<Map<String, Object>> interviews() throws Exception {
    // selects all interviewees that also have a photograph
    return db.executeQuery(new Sql(this).prepare(), Rsc::fetchAll);
  }
}
