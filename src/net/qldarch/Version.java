package net.qldarch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;

@Slf4j
@Bind
@Singleton
public class Version {

  private String version;

  public Version() {
    try (InputStream in = Version.class.getResourceAsStream("version.txt")) {
      try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(in))) {
        version = reader.readLine();
      }
    } catch(IOException e) {
      log.error("failed to load version", e);
      version = "";
    }
  }

  public String get() {
    return version;
  }

  @Override
  public String toString() {
    return get();
  }

}
