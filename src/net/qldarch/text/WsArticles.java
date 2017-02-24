package net.qldarch.text;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.gson.JsonSkipField;
import net.qldarch.jaxrs.ContentType;
import net.qldarch.media.Media;

@Path("/articles")
public class WsArticles {

  @Inject
  private Db db;

  @GET
  @Produces(ContentType.JSON)
  @JsonSkipField(type=Media.class, field="depicts")
  public List<Map<String, Object>> articles() throws Exception {
    return db.executeQuery("select id, label from archobj where type = 'article' and"
        + " deleted is null order by label", Rsc::fetchAll);
  }
}
