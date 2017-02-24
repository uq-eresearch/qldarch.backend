package net.qldarch.media;

import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;

import net.qldarch.hibernate.HS;
import net.qldarch.util.ContentDispositionSupport;

@Path("media/download")
public class WsMediaDownload {

  @Inject
  private HS hs;

  @Inject
  private MediaArchive archive;

  private Response notFound() {
    return Response.status(Status.NOT_FOUND).build();
  }

  private StreamingOutput stream(InputStream in) {
    return out -> IOUtils.copy(in, out);
  }

  @GET
  @Path("/{id}")
  public Response download(@PathParam("id") Long id) {
    Media media = hs.get(Media.class, id);
    if((media != null) && (media.getMimetype() != null) && (!media.isDeleted())) {
      final InputStream in = archive.stream(media);
      if(in != null) {
        return Response.ok(stream(in)).type(media.getMimetype()).header(HttpHeaders.CONTENT_LENGTH,
            media.getFilesize()).header("Content-Disposition", ContentDispositionSupport.attachment(
                media.getFilename())).build();
      }
    }
    return notFound();
  }
}
