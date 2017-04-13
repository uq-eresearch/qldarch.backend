package net.qldarch.relationship;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="relationship")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(of={"id"})
public class Relationship {

  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  private Long subject;

  private Long object;

  @Enumerated(EnumType.STRING)
  private RelationshipType type;

  @Enumerated(EnumType.STRING)
  private RelationshipSource source;

  private String note;

  @Column(name="fromyear")
  private Integer from;

  @Column(name="untilyear")
  private Integer until;

  private Timestamp created;

  private Long owner;
}
