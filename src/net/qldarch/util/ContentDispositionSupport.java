package net.qldarch.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ParseException;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

public class ContentDispositionSupport {

  private static final String SHORT = 
      "attachment; filename=\"%s\";";
  private static final String FULL = 
      "attachment; filename=\"%s\"; filename*=UTF-8''%s";

  public static String attachment(String filename) {
    final String fn_iso8859_1;
    try {
      fn_iso8859_1 = new String(filename.getBytes("ISO-8859-1"), "ISO-8859-1");
    } catch (UnsupportedEncodingException e) {
      return String.format(SHORT, filename);
    }
    final String fn_urlencoded;
    try {
      fn_urlencoded = new URI(null, filename, null).toASCIIString();
      return String.format(FULL, fn_iso8859_1, fn_urlencoded);
    } catch(URISyntaxException e) {
      return String.format(SHORT, fn_iso8859_1);
    }
  }

  private static String getParam0(String name, MultivaluedMap<String, String> headers) {
    if(headers != null) {
      List<String> values = headers.get(name);
      return (values!=null)&&(values.size()>0)?values.get(0):null;
    } else {
      return null;
    }
  }

  public static ContentDisposition contentDisposition(MultivaluedMap<String, String> headers) {
    try {
      String s = getParam0("Content-Disposition", headers);
      return StringUtils.isNotBlank(s)?new ContentDisposition(s):null;
    } catch(ParseException e) {
      throw new RuntimeException("failed to retrieve content disposition header", e);
    }
  }

}
