package net.qldarch.util;

import org.apache.commons.lang3.StringUtils;

public class UrlSupport {

  private static final String BASE = "http://qldarch.net";

  public static String url(String s) {
    if(StringUtils.isNotBlank(s)) {
      if(StringUtils.startsWith(s, "http")) {
        return s;
      } else if(StringUtils.contains(s, "/")) {
        return BASE + "/files/" + s;
      } else {
        return BASE + "/omeka/archive/files/" + s;
      }
    } else {
      return s;
    }
  }

}
