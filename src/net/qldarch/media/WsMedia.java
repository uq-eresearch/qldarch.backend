package net.qldarch.media;

import java.awt.Dimension;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.io.ByteStreams;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;
import net.qldarch.jaxrs.ResponseHeader;

@Path("media")
@Slf4j
public class WsMedia {

  @Inject
  private MediaArchive archive;

  @Inject
  private HS hs;

  @Inject
  private ThumbnailCache tc;

  private StreamingOutput stream(InputStream in) {
    return out -> IOUtils.copy(in, out);
  }
  
  private Response notFound() {
    return Response.status(Status.NOT_FOUND).build();
  }

  private Long parseLong(String s) {
    try {
      return Long.parseLong(s);
    } catch(Exception e) {
      return null;
    }
  }

  private Pair<Long, Long> range(String range) {
    if(range == null) {
      return null;
    }
    Pattern p = Pattern.compile("bytes=(\\d+)-(\\d*)");
    Matcher m = p.matcher(range);
    if(m.matches()) {
      return Pair.of(parseLong(m.group(1)), parseLong(m.group(2)));
    } else {
      return null;
    }
  }

  private boolean rangeOk(String range) {
    return range(range) != null;
  }

  private Response full(InputStream in, String mimetype, long contentLength, String hash, Request req) {
    final CacheControl cc = new CacheControl();
    cc.setMaxAge(60);
    final EntityTag etag = new EntityTag(hash);
    final Response.ResponseBuilder rb = req.evaluatePreconditions(etag);
    if(rb != null) {
      return rb.cacheControl(cc).tag(etag).build();
    } else {
      return Response.ok(stream(in)).type(mimetype).header(HttpHeaders.CONTENT_LENGTH,
          contentLength).cacheControl(cc).tag(etag).build();
    }
  }

  private Response partial(InputStream in, String mimetype, long contentLength, String hash,
      Request req, String strRange) {
    Pair<Long, Long> range = range(strRange);
    if(range == null) {
      return full(in, mimetype, contentLength, hash, req);
    } else {
      Long start = range.getLeft();
      Long end = range.getRight()!=null?range.getRight():contentLength-1;
      long newContentLength = end-start+1;
      try {
        final EntityTag etag = new EntityTag(hash);
        ByteStreams.skipFully(in, start);
        return Response.status(206).entity(stream(ByteStreams.limit(in, newContentLength))).type(
            mimetype).header(HttpHeaders.CONTENT_LENGTH, newContentLength).header("Content-Range",
                String.format("bytes %s-%s/%s", start, end, contentLength)).tag(etag).build();
      } catch(Exception e) {
        log.debug("caught exception while doing partial media response, sending 416", e);
        return Response.status(416).build();
      }
    }
  }

  private Response ok(InputStream in, String mimetype, long contentLength, String hash, Request req,
      String ifRange, String range) {
    if(StringUtils.isNotBlank(ifRange) && 
        StringUtils.equals(StringUtils.strip(ifRange, "\""), hash) && rangeOk(range)) {
      return partial(in, mimetype, contentLength, hash, req, range);
    } else if(StringUtils.isBlank(ifRange) && rangeOk(range)) {
      return partial(in, mimetype, contentLength, hash, req, range);
    } else {
      return full(in, mimetype, contentLength, hash, req);
    }
  }

  private Thumbnail thumbnail(Media media, Dimension dimension) {
    if(dimension!=null && tc.canThumbnail(media, dimension)) {
      final CompletableFuture<Thumbnail> promise = tc.get(media, dimension);
      try {
        return promise.getNow(null);
      } catch(Exception e) {}
    }
    return null;
  }

  @GET
  @Path("/{id}")
  @ResponseHeader("Accept-Ranges:bytes")
  public Response get(@PathParam("id") Long id, @QueryParam("dimension") Dimension dimension,
      @Context Request req, @HeaderParam("If-Range") String ifRange, @HeaderParam("Range") String range) {
    Media media = hs.get(Media.class, id);
    if(media != null) {
      if(media.isDeleted()) {
        return notFound();
      }
      final Thumbnail thumbnail = thumbnail(media, dimension);
      if(thumbnail != null) {
        return ok(thumbnail.getContentprovider().content(), thumbnail.getMimetype(),
          thumbnail.getFilesize(), thumbnail.getHash(), req, ifRange, range);
      } else if(StorageType.External.equals(media.getStoragetype()) ||
          StorageType.ObjectStore.equals(media.getStoragetype())) {
        try {
          return Response.seeOther(new URI(media.getUrl())).build();
        } catch(Exception e) {
          throw new RuntimeException(String.format("failed to send redirect for media %s/%s",
              media.getId(), media.getUrl()));
        }
      } else if(StorageType.Local.equals(media.getStoragetype())) {
        final InputStream in = archive.stream(media);
        if(in != null) {
          return ok(in, media.getMimetype(), media.getFilesize(), media.getHash(), req, ifRange, range);
        } else {
          return notFound();
        }
      } else {
        throw new RuntimeException(String.format("unknown storage type for media %s/%s",
            media.getId(), media.getStoragetype()));
      }
    } else {
      return notFound();
    }
  }
}
