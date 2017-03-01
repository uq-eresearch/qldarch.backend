package net.qldarch.media;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.qldarch.configuration.Cfg;
import net.qldarch.guice.Bind;
import net.qldarch.hibernate.HS;
import net.qldarch.resteasy.Lifecycle;
import net.qldarch.util.Md5HashUtil;

@Bind
@Singleton
@Slf4j
public class ThumbnailCache implements Lifecycle {

  @AllArgsConstructor
  @EqualsAndHashCode
  private static class TKey {
    private final Media media;
    private final Dimension d;
  }

  @Inject @Cfg("thumbnail.cache")
  private String thumbnailCache;

  @Inject
  private HS hs;

  @Inject
  private Mimetypes mimetypes;

  @Inject
  private MediaArchive archive;

  private final Map<TKey, CompletableFuture<Thumbnail>> tnPromises = new HashMap<>();

  private final AtomicInteger tcounter = new AtomicInteger();

  private final ExecutorService executor = Executors.newFixedThreadPool(10, r -> {
    return new Thread(r, String.format("thumbnail_cache_worker_%s", tcounter.getAndIncrement()));
  });

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

  public boolean canThumbnail(Media media, Dimension d) {
    return dimensionOk(d) && (media != null) && isImage(media.getMimetype());
  }

  private File thumbnailCache() {
    File fThumbnailCache = new File(thumbnailCache);
    if(!fThumbnailCache.exists()) {
      fThumbnailCache.mkdirs();
    }
    if(!fThumbnailCache.exists()) {
      throw new RuntimeException("could not create thumbnail cache folder " +
          fThumbnailCache.getAbsolutePath());
    }
    return fThumbnailCache;
  }

  private File thumbnailFile(Media media, Dimension d) {
    final String suffix = mimetypes.suffix(media.getMimetype());
    if(StringUtils.isBlank(suffix)) {
      throw new RuntimeException(String.format("failed to determine suffix for mimetype %s (media %s)",
          media.getMimetype(), media.getId()));
    }
    File fThumb = new File(thumbnailCache(), String.format("%08x_%s_%s.%s",
        media.getId(), d.width, d.height, suffix));
    return fThumb;
  }

  private File createThumbnailFile(Media media, Dimension d) throws IOException {
    File fThumb = thumbnailFile(media, d);
    if(fThumb.exists()) {
      FileUtils.deleteQuietly(fThumb);
    }
    try(InputStream in = archive.stream(media)) {
      try(FileOutputStream fout = new FileOutputStream(fThumb)) {
        Thumbnails.of(in).crop(Positions.CENTER).size(d.width, d.height).toOutputStream(fout);
      }
    }
    return fThumb;
  }

  private Thumbnail createThumbnailBean(File f, Media media, Dimension d) {
    try {
      Thumbnail t = new Thumbnail();
      t.setMedia(media);
      t.setWidth(d.width);
      t.setHeight(d.height);
      t.setHash(Md5HashUtil.hash(f));
      t.setCreated(new Date());
      t.setPath(f.getName());
      t.setMimetype(media.getMimetype());
      t.setFilesize(f.length());
      hs.save(t);
      return t;
    } catch(IOException e) {
      throw new RuntimeException(String.format(
          "failed to create thumbnail entry for media %s, dimension %s", media.getId(), d));
    }
  }

  private Thumbnail getThumbnailBean(Media media, Dimension d) {
    try {
      return hs.execute(session -> session.createQuery("from Thumbnail t where t.media = :media and "
          + "t.width = :width and t.height = :height", Thumbnail.class).setParameter("media",
              media).setParameter("width", d.width).setParameter("height", d.height).getSingleResult());
    } catch(Exception e) {}
    return null;
  }

  private ContentProvider content(final File f) {
    return () -> {
      try {
        return new FileInputStream(f);
      } catch(Exception e) {
        throw new RuntimeException(String.format("failed to fetch content for '%s'", f.getName()), e);
      }
    };
  }

  public CompletableFuture<Thumbnail> get(final Media media, final Dimension d) {
    synchronized(tnPromises) {
      final TKey key = new TKey(media, d);
      final CompletableFuture<Thumbnail> promise = tnPromises.get(key);
      if(promise != null) {
        return promise;
      } else if(canThumbnail(media, d)) {
        Thumbnail t = getThumbnailBean(media, d);
        final File f = thumbnailFile(media, d);
        if((t == null) && f.exists()) {
          t = createThumbnailBean(f, media, d);
        }
        final CompletableFuture<Thumbnail> cf = new CompletableFuture<>();
        if((t != null) && f.exists()) {
          t.setContentprovider(content(f));
          cf.complete(t);
        } else {
          tnPromises.put(key, cf);
          executor.execute(() -> {
            try {
              log.debug("create thumbnail for media id '{}', dimension '{}x{}'",
                  media.getId(), d.width, d.height);
              if(!f.exists()) {
                createThumbnailFile(media, d);
              }
              Thumbnail thumbnail = getThumbnailBean(media, d);
              if(thumbnail == null) {
                thumbnail = createThumbnailBean(f, media, d);
              }
              thumbnail.setContentprovider(content(f));
              cf.complete(thumbnail);
            } catch(Exception e) {
              log.debug("failed to create thumbnail for media id {}, dimension {}", media.getId(), d, e);
              cf.completeExceptionally(e);
            } finally {
              synchronized(tnPromises) {
                tnPromises.remove(key);
              }
              log.debug("finished thumbnail for media id '{}', dimension '{}x{}'",
                  media.getId(), d.width, d.height);
            }
          });
        }
        return cf;
      }
      return null;
    }
  }

  @Override
  public void start() {
    log.debug("start thumbnail cache");
  }

  @Override
  public void stop() {
    log.debug("stop thumbnail cache");
    executor.shutdownNow();
  }

}
