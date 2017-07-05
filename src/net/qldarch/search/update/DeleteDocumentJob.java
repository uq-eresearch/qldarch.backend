package net.qldarch.search.update;

import org.apache.lucene.document.LongPoint;
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

  private final Long id;

  private final String type;

  private Query query(Long id, String type) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    Query i = LongPoint.newExactQuery("id", id);
    Query t = new TermQuery(new Term("type", type));
    builder.add(i, BooleanClause.Occur.MUST);
    builder.add(t, BooleanClause.Occur.MUST);
    return builder.build();
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
