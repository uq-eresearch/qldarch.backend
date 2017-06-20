package net.qldarch.compobj;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.qldarch.archobj.ArchObj;
import net.qldarch.gson.JsonExclude;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Where;

@Entity
@Table(name="timelineevent")
@Data
@EqualsAndHashCode(of={"id"})
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class TimelineEvent {

  @Id
  @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @JsonExclude
  private Long compobj;

  @JsonExclude
  @Column(name="archobj")
  private Long obj;

  @Column(name="fromyear")
  private Integer startDate;

  @Column(name="untilyear")
  private Integer endDate;

  @Column(name="label")
  private String headline;

  @Column(name="note")
  private String text;

  @ManyToOne
  @JoinColumn(
      name="archobj",
      insertable=false,
      updatable=false)
  @Where(clause="deleted is null")
  private ArchObj archobj;

  @JsonCreator
  public TimelineEvent(@JsonProperty("archobj") Long archObj, @JsonProperty("startDate") Integer startdate,
      @JsonProperty("endDate") Integer enddate, @JsonProperty("headline") String head,
      @JsonProperty("text") String txt) {
    obj = archObj;
    startDate = startdate;
    endDate = enddate;
    headline = head;
    text = txt;
  }

}
