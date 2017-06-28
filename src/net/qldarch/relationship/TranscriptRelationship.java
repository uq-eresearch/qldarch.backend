package net.qldarch.relationship;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class TranscriptRelationship implements Comparable<TranscriptRelationship> {

  private Long id;

  private String relationship;

  private String note;

  private Integer from;

  private Integer until;

  private Long subject;

  private String subjectlabel;

  private String subjecttype;

  private boolean subjectarchitect;

  private Long object;

  private String objectlabel;

  private String objecttype;

  private boolean objectarchitect;

  private Long utterance;

  private Timestamp created;

  @Override
  public int compareTo(TranscriptRelationship tr1) {
    if((this.created != null) && (tr1.created != null)) {
      return this.created.compareTo(tr1.created);
    } else if((this.created == null) && (tr1.created == null)) {
      return 0;
    } else if((this.created == null)) {
      return -1;
    } else {
      return 1;
    }
  }

}
