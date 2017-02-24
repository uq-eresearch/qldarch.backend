package net.qldarch.search.update;

import org.apache.lucene.index.IndexWriter;

public interface IndexUpdateJob {
  void run(IndexWriter writer);
  void cancel();
}
