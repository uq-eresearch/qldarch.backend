package net.qldarch.media;

import java.io.InputStream;

@FunctionalInterface
public interface ContentProvider {
  InputStream content();
}
