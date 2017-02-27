package net.qldarch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateUtil {

  public static final String YYYY_MM_DD = "yyyy-MM-dd";

  public static java.sql.Date toSqlDate(Date date) {
    return date!=null?new java.sql.Date(date.getTime()):null;
  }

  public static Date guess(String s) {
    if(StringUtils.isNotBlank(s)) {
      if(s.length() == 4) {
        return parseYear(s);
      } else if(s.length() == 10) {
        return parseYMD(s);
      } else {
        return parseStdDateFormat(s);
      }
    } else {
      return null;
    }
  }

  public static Date parseDate(String s, String format) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat(format);
      return sdf.parse(s);
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Date parseStdDateFormat(String s) {
    return parseDate(s, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  }

  private static Date parseYear(String s) {
    return parseDate(s, "yyyy");
  }

  private static Date parseYMD(String s) {
    return parseDate(s, "yyyy-MM-dd");
  }

}
