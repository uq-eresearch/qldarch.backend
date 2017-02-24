package net.qldarch.search;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.gson.JsonFixedLengthDecimal;
import net.qldarch.gson.serialize.JsonSerializer;
import net.qldarch.jaxrs.ContentType;


@Path("/search")
@Slf4j
public class WsSearch {

  @Inject
  private Search search;

  @Inject
  private Index index;

  @GET
  @Produces(ContentType.JSON)
  @JsonSerializer(type=Document.class, serializer=DocumentTypeAdapter.class)
  @JsonFixedLengthDecimal
  public SearchResult  get(@QueryParam("q") String q,
      @DefaultValue("0") @QueryParam("p") int p,
      @DefaultValue("20") @QueryParam("pc") int pc) {
    try(Directory directory = index.directory()) {
      return search.search(q, p, pc, directory);
    } catch(IOException e) {
      log.warn("exception on search", e);
      throw new RuntimeException(e);
    }
  }

}
