package net.qldarch.search.update;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.index.IndexWriter;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.archobj.ArchObj;
import net.qldarch.db.Db;
import net.qldarch.hibernate.HS;
import net.qldarch.media.Media;
import net.qldarch.media.MediaArchive;

@Slf4j
public class UpdateAllJob extends CancelableIndexUpdateJob {

  @Inject
  private Db db;

  @Inject
  private HS hs;

  @Inject
  private MediaArchive archive;

  private List<Long> getIds(ResultSet rset) {
    List<Long> l = new ArrayList<>();
    try {
      while(rset.next()) {
        l.add(rset.getLong(1));
      }
    } catch(Exception e) {
      throw new RuntimeException("failed to retrieve archobj ids", e);
    }
    return l;
  }

  private void update(IndexWriter writer, ArchObj archobj) {
    if(archobj != null) {
      new UpdateArchObjJob(archobj).run(writer);
    }
  }

  private void update(IndexWriter writer, Media media) {
    if(media != null) {
      new UpdateMediaJob(media, archive).run(writer);
    }
  }

  private void updateArchObjs(IndexWriter writer) {
    try {
      for(Long id : db.executeQuery("select id from archobj where deleted is null", this::getIds)) {
        if(isCanceled()) {
          break;
        }
        update(writer, hs.get(ArchObj.class, id));
      }
    } catch(Exception e) {
      throw new RuntimeException("failed to update search index update on archive object", e);
    }
  }

  private void updateMedia(IndexWriter writer) {
    try {
      for(Long id : db.executeQuery("select id from media where deleted is null", this::getIds)) {
        if(isCanceled()) {
          break;
        }
        update(writer, hs.get(Media.class, id));
      }
    } catch(Exception e) {
      throw new RuntimeException("failed to update search index update on media object", e);
    }
  }

  @Override
  public void run(IndexWriter writer) {
    hs.executeVoid(session -> {
      try {
        log.info("start search index update all");
        if(!isCanceled()) {
          writer.deleteAll();
        }
        if(!isCanceled()) {
          updateArchObjs(writer);
        }
        if(!isCanceled()) {
          updateMedia(writer);
        }
        if(!isCanceled()) {
          writer.commit();
          log.info("finished search index update all");
        } else {
          writer.rollback();
          log.info("canceled search index update all");
        }
        writer.close();
      } catch(Exception e) {
        throw new RuntimeException("failed search index update all run", e);
      }
    });
  }
}
