package net.qldarch.search.update;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.qldarch.media.Media;
import net.qldarch.media.MediaArchive;

@AllArgsConstructor
@Slf4j
public class UpdateMediaJob extends CancelableIndexUpdateJob {

  private Media media;

  private MediaArchive archive;

  private boolean canExtractText(Media media) {
    final String mime = media.getMimetype();
    return 
        "application/pdf".equals(mime) ||
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mime) ||
        "text/plain".equals(mime) ||
        "application/rtf".equals(mime) ||
        "application/msword".equals(mime) ||
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mime);
  }

  private String extract(InputStream in) {
    try {
      AutoDetectParser parser = new AutoDetectParser();
      BodyContentHandler handler = new BodyContentHandler();
      Metadata metadata = new Metadata();
      parser.parse(in, handler, metadata);
      return handler.toString();
    } catch(Exception e) {
      return null;
    }
  }

  private String getText(Media media) {
    if(canExtractText(media)) {
      try(InputStream in = archive.stream(media)) {
        return extract(in);
      } catch(Exception e) {
        log.debug("could not extract text from {}", media.getFilename(), e);
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public void run(IndexWriter writer) {
    if(media != null) {
      try {
        if(!isCanceled()) {
          new DeleteDocumentJob(media.getId().toString(), media.getType().toString()).run(writer);
          Map<String, Object> m = media.asMap();
          final String text = getText(media);
          if(StringUtils.isNotBlank(text)) {
            m.put("all", text);
          }
          m.put("category", "media");
          writer.addDocument(DocumentUtils.createDocument(m));
        }
      } catch(Exception e) {
        throw new RuntimeException("failed to update index with media object "+media.getId());
      }
    }
  }

}
