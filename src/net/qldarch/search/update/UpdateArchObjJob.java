package net.qldarch.search.update;

import java.util.Map;

import org.apache.lucene.index.IndexWriter;

import lombok.AllArgsConstructor;
import net.qldarch.archobj.ArchObj;

@AllArgsConstructor
public class UpdateArchObjJob extends CancelableIndexUpdateJob {

  private ArchObj archobj;

  @Override
  public void run(IndexWriter writer) {
    if(archobj != null) {
      try {
        if(!isCanceled()) {
          new DeleteDocumentJob(archobj.getId().toString(), archobj.getType().toString()).run(writer);
          Map<String, Object> m = archobj.asMap();
          m.put("category", "archobj");
          writer.addDocument(DocumentUtils.createDocument(m));
        }
      } catch(Exception e) {
        throw new RuntimeException("failed to update index with archive object "+archobj.getId(), e);
      }
    }
  }
}
