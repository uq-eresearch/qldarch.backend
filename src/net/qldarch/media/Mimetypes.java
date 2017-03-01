package net.qldarch.media;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import net.qldarch.guice.Bind;

@Bind
@Singleton
public class Mimetypes {

  private final Map<String, String> mimetypes = new HashMap<>();

  public Mimetypes() {
    mimetypes.put("application/msword", "doc");
    mimetypes.put("application/pdf", "pdf");
    mimetypes.put("application/rtf", "rtf");
    mimetypes.put("application/vnd.ms-excel", "xls");
    mimetypes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    mimetypes.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
    mimetypes.put("image/gif", "gif");
    mimetypes.put("image/jpeg", "jpeg");
    mimetypes.put("image/png", "png");
    mimetypes.put("image/tiff", "tiff");
    mimetypes.put("text/plain", "txt");
    mimetypes.put("audio/mpeg", "mp3");
    mimetypes.put("audio/ogg", "ogg");
  }

  public String suffix(String mimetype) {
    return mimetypes.get(mimetype);
  }

  public String mimetype(File f) {
    try {
      return Files.probeContentType(f.toPath());
    } catch(Exception e) {
      return null;
    }
  }

}
