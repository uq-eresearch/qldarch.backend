package net.qldarch.structure;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;

@Path("/buildingtypologies")
public class WsBuildingTypologies {

  @Inject
  private BuildingTypologies buildingTypologies;

  @GET
  @Produces(ContentType.JSON)
  public Map<Integer, String> get() {
    Set<String> items = buildingTypologies.getTypes();
    Iterator<String> it = items.iterator();
    return IntStream.range(0, items.size()).boxed().collect(Collectors.toMap(i -> i, i -> it.next()));
  }
}
