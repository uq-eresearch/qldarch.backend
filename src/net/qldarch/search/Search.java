package net.qldarch.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import net.qldarch.guice.Bind;

@Bind
public class Search {

  public SearchResult search(String q, int page, int pagecount, Directory d)  {
    try(DirectoryReader ireader = DirectoryReader.open(d)) {
      IndexSearcher isearcher = new IndexSearcher(ireader);
      // TODO somehow add the ids to query to improve performance
      // (e.g. in this case we would get around fetching every document and filter by id afterwards,
      // also the topdocs.totalHits would be correct and also the search function can be 
      // passed from+pagecount as a limit instead of Integer.MAX_VALUE) 
      TopDocs topdocs = isearcher.search(buildQuery(q), Integer.MAX_VALUE);
      List<Document> documents = Arrays.stream(topdocs.scoreDocs).map(sdoc -> {
        try {
          return isearcher.doc(sdoc.doc);
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }).filter(Objects::nonNull).collect(Collectors.toList());
      final int totalHits = documents.size();
      return new SearchResult(totalHits, page, pagecount, documents.stream().skip(
          page * pagecount).limit(pagecount).collect(Collectors.toList()));
    } catch(Exception e) {
      return new SearchResult(0, page, pagecount, Collections.emptyList());
    }
  }

  private Query buildQuery(final String q) throws ParseException {
    Analyzer analyzer = new StandardAnalyzer();
    QueryParser parser = new QueryParser("all", analyzer);
    parser.setDefaultOperator(QueryParser.Operator.AND);
    parser.setAllowLeadingWildcard(true);
    return parser.parse(q);
  }

}
