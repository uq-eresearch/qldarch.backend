package net.qldarch.search.update;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class DeleteDocumentJob extends CancelableIndexUpdateJob {

  private final String id;

  private final String type;

  private Query query(String id, String type) {
    return new BooleanQuery.Builder()
        .add(new TermQuery(new Term("id", id)), BooleanClause.Occur.MUST)
        .add(new TermQuery(new Term("type", type)), BooleanClause.Occur.MUST)
        .build();
  }

  @Override
  public void run(IndexWriter writer) {
    try {
      if(!isCanceled()) {
        writer.deleteDocuments(query(id, type));
      }
    } catch(Exception e) {
      log.debug("failed to remove document for id {}, type {}", id, type);
    }
  }
}
