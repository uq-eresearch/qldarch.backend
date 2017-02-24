package net.qldarch.search.update;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.qldarch.jaxrs.ContentType;
import net.qldarch.security.Admin;

@Path("/search/updateall")
public class WsUpdateAll {

  @Inject
  private IndexUpdater updater;

  @Inject
  private UpdateAllJob updateAllJob;

  @POST
  @Produces(ContentType.TEXT_PLAIN)
  @Admin
  public boolean updateAll() {
    return updater.addTasks(updateAllJob);
  }

}
