package net.qldarch.relationship;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="interviewrelationship")
@Data
@EqualsAndHashCode(callSuper=true)
public class InterviewRelationship extends Relationship {

  private Long interview;

  private Long utterance;

}
