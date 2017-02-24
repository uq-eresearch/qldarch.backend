package net.qldarch.search;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import net.qldarch.configuration.Cfg;
import net.qldarch.guice.Bind;

@Bind
@Singleton
public class Index {

  private final File indexDir;

  @Inject
  public Index(@Cfg("index") String index) {
    indexDir = new File(index);
    if(!indexDir.exists()) {
      throw new RuntimeException(String.format("index directory '%s' does not exist", index));
    }
    if(!indexDir.isDirectory()) {
      throw new RuntimeException(String.format("index directory '%s' is not a directory", index));
    }
    if(!indexDir.canWrite()) {
      throw new RuntimeException(String.format(
          "can not write to index directory '%s' check permissions", index));
    }
  }

  public Directory directory() {
    try {
      return FSDirectory.open(indexDir.toPath());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

}
