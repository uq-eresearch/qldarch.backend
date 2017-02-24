package net.qldarch.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class HttpSupport {

  public static String fetchAsString(final String url) {
    try {
      URLConnection con = new URL(url).openConnection();
      try(InputStream in = con.getInputStream()) {
        return  IOUtils.toString(in, Charset.forName("UTF-8"));
      } finally {
        ((HttpURLConnection)con).disconnect();
      }
    } catch(IOException e) {
      return null;
    }
  }

  public static byte[] fetchAsBytes(final String url) {
    try {
      URLConnection con = new URL(url).openConnection();
      try(InputStream in = con.getInputStream()) {
        return  IOUtils.toByteArray(in);
      } finally {
        ((HttpURLConnection)con).disconnect();
      }
    } catch(IOException e) {
      return null;
    }
  }
}
