package net.qldarch.util;

import java.util.HashMap;

import javax.inject.Singleton;

import net.qldarch.guice.Bind;

@Bind
@Singleton
public class MimeUtils {

  private HashMap<String, String> mimetypes = new HashMap<>();

  public MimeUtils() {
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
  }

  public String getSuffix(String mimetype) {
    return mimetypes.get(mimetype);
  }

}
