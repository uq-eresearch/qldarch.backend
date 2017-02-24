package net.qldarch.search;

import java.util.List;

import org.apache.lucene.document.Document;

public class SearchResult {

  public int hits;

  public int page;

  public int pagecount;

  public List<Document> documents;

  public SearchResult(int hits, int page, int pagecount, List<Document> documents) {
    this.hits = hits;
    this.page = page;
    this.pagecount = pagecount;
    this.documents = documents;
  }
}
