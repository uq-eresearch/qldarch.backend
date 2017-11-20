package net.qldarch.search.update;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import net.qldarch.guice.Bind;
import net.qldarch.search.Index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

@Bind
@Singleton
public class SearchIndexWriter {

  @Getter
  private final IndexWriter writer;

  @Inject
  public SearchIndexWriter(Index index) {
    try {
      Directory directory = index.directory();
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      writer = new IndexWriter(directory, config);
    } catch(IOException e) {
      throw new RuntimeException("failed to init search index writer", e);
    }
  }

}
