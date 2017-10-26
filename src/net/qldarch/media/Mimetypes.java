package net.qldarch.media;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import net.qldarch.guice.Bind;

import org.apache.tika.Tika;

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
    mimetypes.put("video/x-flv", "flv");
    mimetypes.put("video/mp4", "mp4");
    mimetypes.put("application/x-mpegURL", "m3u8");
    mimetypes.put("video/MP2T", "ts");
    mimetypes.put("video/3gpp", "3gp");
    mimetypes.put("video/quicktime", "mov");
    mimetypes.put("video/x-msvideo", "avi");
    mimetypes.put("video/x-ms-wmv", "wmv");
    mimetypes.put("video/mpeg", "mpeg");
    mimetypes.put("video/ogg", "ogv");
    mimetypes.put("video/webm", "webm");
    mimetypes.put("video/3gpp2", "3g2");
  }

  public String suffix(String mimetype) {
    return mimetypes.get(mimetype);
  }

  public String mimetype(File f) {
    try {
      String mimetype = Files.probeContentType(f.toPath());
      if(mimetypes.containsKey(mimetype)) {
        return mimetype;
      } else {
        return new Tika().detect(f);
      }
    } catch(Exception e) {
      return null;
    }
  }

}
