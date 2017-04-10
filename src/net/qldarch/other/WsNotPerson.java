package net.qldarch.other;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.jaxrs.ContentType;

@Path("others/notperson")
public class WsNotPerson {

  @Inject
  private Db db;

  @GET
  @Produces(ContentType.JSON)
  public List<Map<String, Object>> get() throws Exception {
    return db.executeQuery("select id, label, summary, note, type from archobj where type != 'article' "
        + "and type != 'person' and type != 'firm' and type != 'structure' "
        + "and type != 'interview' and deleted is null order by id", Rsc::fetchAll);
  }
}
