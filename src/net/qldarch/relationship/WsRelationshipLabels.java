package net.qldarch.relationship;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;

@Path("/relationship/labels")
public class WsRelationshipLabels {

  @GET
  @Produces(ContentType.JSON)
  public Map<String, String> get() {
    return Arrays.stream(RelationshipType.values()).collect(
        Collectors.toMap(rt -> rt.name(), rt -> rt.label()));
  }
}
