package net.qldarch;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/version")
public class WsVersion {

  @Inject
  private Version version;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String version() {
    return version.get();
  }

}
