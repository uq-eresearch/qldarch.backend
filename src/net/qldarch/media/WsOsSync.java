package net.qldarch.media;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.Admin;

@Path("ossync")
public class WsOsSync {

  @Inject
  private ObjectStoreSync osSync;

  @GET
  @Produces(ContentType.TEXT_PLAIN)
  @Admin
  public String sync() {
    osSync.wake();
    return "ok";
  }
}
