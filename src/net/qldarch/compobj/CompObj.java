package net.qldarch.compobj;

import java.sql.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.qldarch.security.User;
import net.qldarch.structure.Structure;

@Entity
@Table(name="compobj")
@Data
@EqualsAndHashCode(of={"id"})
@ToString
public class CompObj {

  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(name="label")
  private String title;

  @Enumerated(EnumType.STRING)
  private CompObjType type;

  @Column(name="created")
  private Date modified;

  private Long owner;

  @ManyToMany
  @JoinTable(
      name="compobjstructure",
      joinColumns=@JoinColumn(name="compobj"),
      inverseJoinColumns=@JoinColumn(name="structure"))
  private Set<Structure> structure;

  @OneToMany(mappedBy="compobj")
  private Set<TimelineEvent> timelineevent;

  @OneToMany(mappedBy="compobj")
  private Set<WordCloud> wordcloud;

  @ManyToOne
  @JoinColumn(
      name="owner",
      insertable=false,
      updatable=false)
  private User user;

}
