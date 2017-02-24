package net.qldarch.interview;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;
import net.qldarch.person.Person;

@Entity
@Table(name="interview")
@Data
@EqualsAndHashCode(callSuper=true, exclude={"interviewee", "interviewer"})
public class Interview extends ArchObj {

  private String location;

  @ManyToMany
  @JoinTable(
      name="interviewee",
      joinColumns=@JoinColumn(name="interview"),
      inverseJoinColumns=@JoinColumn(name="interviewee"))
  private Set<Person> interviewee;

  @ManyToMany
  @JoinTable(
      name="interviewer",
      joinColumns=@JoinColumn(name="interview"),
      inverseJoinColumns=@JoinColumn(name="interviewer"))
  private Set<Person> interviewer;

  @OneToMany(mappedBy="interview")
  @OrderBy("time")
  private SortedSet<Utterance> transcript;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    if(StringUtils.isNotBlank(location)) {
      m.put("location", location);
    }
    return m;
  }

}
