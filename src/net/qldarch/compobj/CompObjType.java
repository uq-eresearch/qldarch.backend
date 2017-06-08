package net.qldarch.compobj;

import org.apache.commons.lang3.StringUtils;

public enum CompObjType {

  timeline, map, wordcloud;

  public static CompObjType of(String type) {
    return StringUtils.isBlank(type)?null:CompObjType.valueOf(type);
  }

}
