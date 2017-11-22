package net.qldarch.media;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;
import net.qldarch.hibernate.HS;
import net.qldarch.resteasy.Lifecycle;
import net.qldarch.util.Md5HashUtil;

@Bind
@Singleton
@Slf4j
public class Thumbnails implements Lifecycle {

  @AllArgsConstructor
  @EqualsAndHashCode
  private static class TKey {
    private final Media media;
    private final Dimension d;
  }

  @Inject
  private HS hs;

  @Inject
  private ThumbnailGenerator generator;

  private final Map<TKey, CompletableFuture<Thumbnail>> tnPromises = new HashMap<>();

  private final AtomicInteger tcounter = new AtomicInteger();

  private final ExecutorService executor = Executors.newFixedThreadPool(10, r -> {
    return new Thread(r, String.format("thumbnail_cache_worker_%s", tcounter.getAndIncrement()));
  });

  public boolean canCreateThumbnail(Media media, Dimension d) {
    return generator.canCreateThumbnail(media, d);
  }

  private Thumbnail findExact(Media media, Dimension d) {
    try {
      return hs.execute(session -> session.createQuery("from Thumbnail t where t.media = :media and "
          + "t.width = :width and t.height = :height", Thumbnail.class).setParameter("media",
              media).setParameter("width", d.width).setParameter("height", d.height).getSingleResult());
    } catch(Exception e) {}
    return null;
  }

  private Thumbnail findLargest(Media media) {
    try {
      return hs.execute(session -> session.createQuery(
          "from Thumbnail t where t.media = :media order by t.filesize desc",
          Thumbnail.class).setParameter("media",media).setMaxResults(1).getSingleResult());
    } catch(Exception e) {}
    return null;
  }

  private Thumbnail findThumbnail(final Media media, final Dimension d) {
    Thumbnail exact = findExact(media, d);
    if(exact != null) {
      log.debug("found thumbnail for media id {}, {}x{}", media.getId(), d.width, d.height);
      return exact;
    }
    Thumbnail largest = findLargest(media);
    if(largest != null) {
      log.debug("found thumbnail for media id {}, with different dimensions {}x{}, requested {}x{}",
          media.getId(), largest.getWidth(), largest.getHeight(), d.width, d.height);
      createThumbnail(media, d);
      return largest;
    }
    return null;
  }

  private CompletableFuture<Thumbnail> createThumbnail(final Media media, final Dimension d) {
    synchronized(tnPromises) {
      final CompletableFuture<Thumbnail> cf = new CompletableFuture<>();
      final TKey key = new TKey(media, d);
      tnPromises.put(key, cf);
      executor.execute(() -> {
          Thumbnail t = new Thumbnail();
          t.setMedia(media);
          t.setWidth(d.width);
          t.setHeight(d.height);
          t.setCreated(new Date());
          t.setPath(media.getPath());
          t.setMimetype(media.getMimetype());
          t.setFailed(false);
          try {
            final byte[] buf = generator.createThumbnail(media, d);
            t.setHash(Md5HashUtil.hash(new ByteArrayInputStream(buf)));
            t.setThumbnail(buf);
            t.setFilesize(buf.length);
            hs.save(t);
            cf.complete(t);
          } catch(Exception e) {
            log.debug("failed to generate thumbnail for media id {}, {}x{}",
                media.getId(), d.width, d.height, e);
            cf.completeExceptionally(e);
            t.setFailed(true);
            t.setFailmsg(e.getMessage());
            hs.save(t);
          } finally {
          synchronized(tnPromises) {
            tnPromises.remove(key);
          }
        }
      });
      return cf;
    }
  }

  private CompletableFuture<Thumbnail> findOrCreateThumbnail(final Media media, final Dimension d) {
    Thumbnail t = findThumbnail(media, d);
    if(t != null) {
      final CompletableFuture<Thumbnail> cf = new CompletableFuture<>();
      cf.complete(t.isFailed()?null:t);
      return cf;
    } else {
      return createThumbnail(media, d);
    }
  }

  public CompletableFuture<Thumbnail> get(final Media media, final Dimension d) {
    synchronized(tnPromises) {
      final TKey key = new TKey(media, d);
      final CompletableFuture<Thumbnail> promise = tnPromises.get(key);
      if(promise != null) {
        // if thumbnail creation for media and dimension is currently underway just return that promise
        return promise;
      } else if(canCreateThumbnail(media, d)) {
        return findOrCreateThumbnail(media, d);
      } else {
        return null;
      }
    }
  }

  @Override
  public void start() {
    log.debug("start thumbnail workers");
  }

  @Override
  public void stop() {
    log.debug("stop thumbnail workers");
    executor.shutdownNow();
  }

}
