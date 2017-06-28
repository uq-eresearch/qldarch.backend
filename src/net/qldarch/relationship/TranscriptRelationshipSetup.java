package net.qldarch.relationship;

import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.db.Sql;
import net.qldarch.interview.Interview;
import net.qldarch.interview.Utterance;
import net.qldarch.util.M;

public class TranscriptRelationshipSetup {

  @Inject
  private Db db;

  private Long getLong(Map<String, Object> m, String key) {
    Object o = m.get(key);
    return (o instanceof Number)?((Number)o).longValue():null;
  }

  private Long getLongNotNull(Map<String, Object> m, String key) {
    Long l = getLong(m, key);
    if(l!=null) {
      return l;
    } else {
      throw new RuntimeException(String.format("unexpected null for key %s in map %s", key, m));
    }
  }

  private Integer getInt(Map<String, Object> m, String key) {
    Object o = m.get(key);
    if(o != null) {
      if(o instanceof Number) {
        return ((Number)o).intValue();
      }
      return null;
    } else {
      return null;
    }
  }

  private String getString(Map<String, Object> m, String key) {
    Object o = m.get(key);
    return (o instanceof String)?(String)o:null;
  }

  private String getStringNotNull(Map<String, Object> m, String key) {
    String s = getString(m, key);
    if(s!=null) {
      return s;
    } else {
      throw new RuntimeException(String.format("unexpected null for key %s in map %s", key, m));
    }
  }

  private Timestamp getTimestamp(Map<String, Object> m, String key) {
    Object o = m.get(key);
    return (o instanceof Timestamp)?(Timestamp)o:null;
  }

  private Timestamp getTimestampNotNull(Map<String, Object> m, String key) {
    Timestamp t = getTimestamp(m, key);
    if(t!=null) {
      return t;
    } else {
      throw new RuntimeException(String.format("unexpected null for key %s in map %s", key, m));
    }
  }

  private boolean getBoolean(Map<String, Object> m, String key) {
    Object o = m.get(key);
    return (o instanceof Boolean)?(Boolean)o:false;
  }

  private TranscriptRelationship createTR(Map<String, Object> m) {
    final TranscriptRelationship r = new TranscriptRelationship();
    r.setId(getLongNotNull(m, "relationshipId"));
    r.setRelationship(getStringNotNull(m, "relationship"));
    r.setNote(getString(m, "note"));
    r.setFrom(getInt(m, "fromyear"));
    r.setUntil(getInt(m, "untilyear"));
    r.setSubject(getLongNotNull(m, "subject"));
    r.setSubjectlabel(getStringNotNull(m, "subjectlabel"));
    r.setSubjecttype(getStringNotNull(m, "subjecttype"));
    r.setSubjectarchitect(getBoolean(m, "subjectarchitect"));
    r.setObject(getLongNotNull(m, "object"));
    r.setObjectlabel(getStringNotNull(m, "objectlabel"));
    r.setObjecttype(getStringNotNull(m, "objecttype"));
    r.setObjectarchitect(getBoolean(m, "objectarchitect"));
    r.setUtterance(getLongNotNull(m, "utterance"));
    r.setCreated(getTimestampNotNull(m, "created"));
    return r;
  }

  private Stream<TranscriptRelationship> relationships(Long interviewId) {
    try {
      return db.executeQuery(new Sql("net/qldarch/relationship/TranscriptRelationship.sql").prepare(),
          M.of("interview", interviewId), Rsc::fetchAll).stream().map(this::createTR);
    } catch(Exception e) {
      throw new RuntimeException(
          "failed to retrieve interview relationships for interview with id "+interviewId, e);
    }
  }

  public void setup(Interview interview) {
    if(interview.getTranscript() != null && interview.getTranscript().size() > 0) {
      Map<Long, Utterance> uMap = interview.getTranscript().stream().collect(
          Collectors.toMap(Utterance::getId, u->u));
      relationships(interview.getId()).forEach(ir -> {
        Utterance u = uMap.get(ir.getUtterance());
        if(u!=null) {
          if(u.getRelationships()==null) {
            u.setRelationships(new TreeSet<>());
          }
          u.getRelationships().add(ir);
        }
      });
    }
  }

}
