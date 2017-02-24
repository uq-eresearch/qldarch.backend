package net.qldarch.media;

import java.io.FileInputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.configuration.Cfg;
import net.qldarch.db.Db;
import net.qldarch.guice.Bind;
import net.qldarch.openstack.FileInfo;
import net.qldarch.openstack.Keystone;
import net.qldarch.openstack.ObjectStore;
import net.qldarch.resteasy.Lifecycle;
import net.qldarch.util.M;
import net.qldarch.util.MimeUtils;

@Singleton
@Bind
@Slf4j
public class ObjectStoreSync implements Lifecycle {

  @Inject
  private Keystone keystone;

  @Inject
  private ObjectStore objectstore;

  @Inject
  private Db db;

  @Inject 
  private MediaArchive archive;

  @Inject
  private MimeUtils mimeutils;

  @Inject @Cfg("os.tenant")
  private transient String tenant;

  @Inject @Cfg("os.user")
  private transient String user;

  @Inject @Cfg("os.password")
  private transient String password;

  @Inject @Cfg("os.sync.deleteAfterSync")
  private transient boolean deleteAfterSync;

  private transient String token;

  private volatile boolean start = false;
  private volatile boolean stop = false;

  private final Object sleep = new Object();

  private static enum SyncMode {
    off, updatedb, sync
  }

  private final SyncMode mode;

  private final Thread osSync = new Thread(() -> {
    log.debug("started object store sync");
    sleep(TimeUnit.SECONDS.toMillis(30));
    while(!stop) {
      try {
        int failures = 0;
        log.debug("start sync cycle");
        for(final FileInfo file : getLocalMedia()) {
          if(stop || (failures >= 3)) {
            break;
          }
          try {
            log.debug("now syncing file '{}'", file);
            if(sync(file)) {
              if(deleteAfterSync) {
                log.debug("deleting '{}' from local archive after sync to object store", file.name);
                archive.deleteLocalFile(file.name);
              }
            }
          } catch(Exception e) {
            log.debug("failed to sync file {}", file, e);
            failures++;
          }
        }
        if(stop) {
          break;
        }
        log.debug("finished sync cycle, waiting...");
        sleep(TimeUnit.HOURS.toMillis(1));
      } catch(Exception e) {
        log.warn("failure in object store sync", e);
      }
    }
    log.debug("stopped object store sync");
  }, "ObjectStore Sync");

  @Inject
  public ObjectStoreSync(@Cfg("os.sync.mode") String mode) {
    if(SyncMode.valueOf(mode) == null) {
      log.warn("invalid value for os.sync.mode '{}', check config. default to 'off'", mode);
      this.mode = SyncMode.off;
    } else {
      this.mode = SyncMode.valueOf(mode);
    }
  }

  @Override
  public synchronized void start() {
    if((!SyncMode.off.equals(mode)) && (!start)) {
      start = true;
      osSync.start();
    } else if(SyncMode.off.equals(mode)) {
      log.debug("object store sync disabled in config");
    }
  }

  @Override
  public synchronized void stop() {
    if(start && !stop) {
      stop = true;
      osSync.interrupt();
      try {
        osSync.join();
      } catch(Exception e) {}
    }
  }

  private List<FileInfo> fetchAll(ResultSet rset) {
    try {
      List<FileInfo> files = new ArrayList<>();
      while(rset.next()) {
        files.add(new FileInfo(rset.getLong("id"), rset.getString("path"), rset.getLong("filesize"),
            rset.getString("mimetype"), rset.getString("hash")));
      }
      return files;
    } catch(Exception e) {
      throw new RuntimeException("failed to fetch files from resultset", e);
    }
  }

  private List<FileInfo> getLocalMedia() {
    try {
      return db.executeQuery("select id, path, hash, mimetype, filesize"
          + " from media where storagetype = 'Local'", this::fetchAll);
    } catch(Exception e) {
      log.error("failed to retrieve list of local files", e);
      return Collections.emptyList();
    }
  }

  private boolean upload(String localFilename,FileInfo osFile) {
    try {
      log.debug("upload local file '{}' to object store '{}'", localFilename, osFile.name);
      return objectstore.upload(token, osFile, new FileInputStream(archive.getLocalFile(localFilename)));
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to upload file '%s'", osFile.id), e);
    }
  }

  private boolean probablySameFile(FileInfo f1, FileInfo f2) {
    return f1.name.equals(f2.name) && f1.bytes.equals(f2.bytes) &&
        f1.hash.equals(f2.hash) && f1.mimetype.equals(f2.mimetype);
  }

  private FileInfo info(String filename) {
    log.debug("fetching file info {} from object store", filename);
    return objectstore.info(token, filename);
  }

  private boolean signin() {
    if(!keystone.valid(token)) {
      token = keystone.signin(tenant, user, password);
      return keystone.valid(token);
    } else {
      return true;
    }
  }

  private FileInfo tryUpload(String localFilename, FileInfo file) {
    if(upload(localFilename, file)) {
      return file;
    } else if(signin()) {
      if(upload(localFilename, file)) {
        return file;
      }
    }
    return null;
  }

  private FileInfo trySync(FileInfo file) {
    if(SyncMode.sync.equals(mode)) {
      FileInfo info = info(file.name);
      if(info == null) {
        return tryUpload(file.name, file);
      } else {
        if(probablySameFile(file, info)) {
          return file;
        } else {
          log.debug("hash collision detected on media file with id {}", file.id);
          final String suffix = mimeutils.getSuffix(file.mimetype);
          if(StringUtils.isBlank(suffix)) {
            log.warn("failed to determine suffix for mimetype {}", file.mimetype);
            return null;
          }
          final FileInfo osFile = new FileInfo(file.id, String.format("%08x.%s", file.id, suffix),
              file.bytes, file.mimetype, file.hash);
          return tryUpload(file.name, osFile);
        }
      }
    } else if(SyncMode.updatedb.equals(mode)) {
      FileInfo info = info(file.name);
      if(probablySameFile(file, info)) {
        return file;
      } else {
        return null;
      }
    } else {
      log.info("unknown sync mode '{}'", mode);
      return null;
    }
  }

  private boolean sync(FileInfo file) {
    FileInfo f = trySync(file);
    if(f!=null) {
      try {
        db.execute("update media set storagetype = 'ObjectStore', path = :path where id = :id",
            M.of("id", file.id, "path", f.name));
        log.debug("object store sync with file '{}/{}' successful", file.id, f.name);
        return true;
      } catch(Exception e) {
        log.debug("failed to update media entry with id '{}'"
            + " after successful sync with object store", file.id, e);
      }
    }
    return false;
  }

  private void sleep(long durationMs) {
    synchronized(sleep) {
      try {
        sleep.wait(durationMs);
      } catch(InterruptedException e) {}
    }
  }

  public void wake() {
    synchronized(sleep) {
      sleep.notifyAll();
    }
  }

}
