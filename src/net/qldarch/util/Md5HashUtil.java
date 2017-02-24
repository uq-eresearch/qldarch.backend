package net.qldarch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;

public class Md5HashUtil {

  public static String hash(File f) throws IOException {
    try(FileInputStream in = new FileInputStream(f)) {
      return hash(in);
    }
  }

  public static String hash(InputStream in) throws IOException {
    return DigestUtils.md5Hex(in);
  }
}
