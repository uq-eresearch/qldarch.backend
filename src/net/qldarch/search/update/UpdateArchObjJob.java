package net.qldarch.search.update;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexWriter;

import lombok.AllArgsConstructor;
import net.qldarch.archobj.ArchObj;
import net.qldarch.interview.Interview;
import net.qldarch.interview.Utterance;

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
          if(archobj instanceof Interview) {
            final Set<Utterance> transcript = ((Interview)archobj).getTranscript();
            if(transcript != null) {
              final String text = transcript.stream().map(
                  Utterance::getTranscript).collect(Collectors.joining(" "));
              m.put("all", text);
            }
          }
          writer.addDocument(DocumentUtils.createDocument(m));
        }
      } catch(Exception e) {
        throw new RuntimeException("failed to update index with archive object "+archobj.getId(), e);
      }
    }
  }
}
