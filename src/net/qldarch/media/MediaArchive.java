package net.qldarch.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;

import net.qldarch.configuration.Cfg;

public class MediaArchive {

  @Inject @Cfg("media.archive")
  private String mediaArchive;

  public InputStream stream(Media media) {
    if(media == null) {
      return null;
    }
    if(StorageType.Local.equals(media.getStoragetype())) {
      final File archive = new File(mediaArchive);
      final File f = new File(archive, media.getPath());
      try {
        return new FileInputStream(f);
      } catch(FileNotFoundException e) {
        return null;
      }
    } else if(media.getMimetype() != null) {
      try {
        URL url = new URL(media.getUrl());
        return url.openStream();
      } catch(Exception e) {
        throw new RuntimeException(String.format("failed to stream media with id %s", media.getId()));
      }
    } else {
      return null;
    }
  }

  public File getLocalFile(String path) {
    return new File(new File(mediaArchive), path);
  }

  public void deleteLocalFile(String path) {
    final File f = getLocalFile(path);
    if(f.exists()) {
      f.delete();
    }
  }
}
