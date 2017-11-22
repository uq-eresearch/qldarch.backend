package net.qldarch.media;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.qldarch.guice.Bind;

@Bind
@Singleton
@Slf4j
public class ThumbnailGenerator {

  @Inject
  private MediaArchive archive;

  private boolean isImage(String mimetype) {
    return StringUtils.startsWith(mimetype, "image/");
  }

  private boolean aspectRatioOk(Dimension d) {
    // TODO
    return true;
  }

  private boolean sizeOk(Dimension d) {
    return (d.width > 0) && (d.width <= 1000) && (d.height > 0) && (d.height <= 1000);
  }

  private boolean dimensionOk(Dimension d) {
    return (d != null) && sizeOk(d) && aspectRatioOk(d);
  }

  public boolean canCreateThumbnail(Media media, Dimension d) {
    return dimensionOk(d) && (media != null) && isImage(media.getMimetype());
  }

  public byte[] createThumbnail(Media media, Dimension d) throws IOException {
    try(InputStream in = archive.stream(media)) {
      try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        log.debug("creating thumbnail for media id {}, {}x{}", media.getId(), d.width, d.height);
        Thumbnails.of(in).crop(Positions.CENTER).size(d.width, d.height).toOutputStream(out);
        return out.toByteArray();
      }
    }
  }
}
