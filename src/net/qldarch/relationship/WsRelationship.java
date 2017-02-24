package net.qldarch.relationship;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ContentType;

@Path("/relationship/{id}")
public class WsRelationship {

  @Inject
  private HS hs;

  @GET
  @Produces(ContentType.JSON)
  public Relationship get(@PathParam("id") Long id) {
    return hs.get(Relationship.class, id);
  }
}
