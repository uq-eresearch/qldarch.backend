package net.qldarch.interview;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.util.HashSet;
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
import net.qldarch.util.ObjUtils;

@Entity
@Table(name="interview")
@Data
@EqualsAndHashCode(callSuper=true, exclude={"interviewee", "interviewer", "transcript"})
public class Interview extends ArchObj {

  private static final String LOCATION = "location";
  private static final String INTERVIEWEE = "interviewee";
  private static final String INTERVIEWER = "interviewer";

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
      m.put(LOCATION, location);
    }
    return m;
  }

  @Override
  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = super.updateFrom(m);
    if(hasChanged(m, LOCATION, location)) {
      changed = true;
      location = ObjUtils.asString(m.get(LOCATION));
    }
    if(hasChanged(m, INTERVIEWEE, interviewee)) {
      changed = true;
      Set<Long> intvwees = ObjUtils.asLongSet(m.get(INTERVIEWEE));
      interviewee = new HashSet<>();
      if(intvwees != null) {
        for(Long e : intvwees) {
          Person p = new Person();
          p.setId(e);
          interviewee.add(p);
        }
      }
    }
    if(hasChanged(m, INTERVIEWER, interviewer)) {
      changed = true;
      Set<Long> intvwers = ObjUtils.asLongSet(m.get(INTERVIEWER));
      interviewer = new HashSet<>();
      if(intvwers != null) {
        for(Long r : intvwers) {
          Person p = new Person();
          p.setId(r);
          interviewer.add(p);
        }
      }
    }
    return changed;
  }

  @Override
  public void copyFrom(Map<String, Object> m) {
    super.copyFrom(m);
    location = ObjUtils.asString(m.get(LOCATION));
    Set<Long> intvwees = ObjUtils.asLongSet(m.get(INTERVIEWEE));
    Set<Long> intvwers = ObjUtils.asLongSet(m.get(INTERVIEWER));
    if(intvwees != null && interviewee == null) {
      interviewee = new HashSet<>();
    }
    if(intvwers != null && interviewer == null) {
      interviewer = new HashSet<>();
    }
    if(intvwees != null) {
      for(Long e : intvwees) {
        Person p = new Person();
        p.setId(e);
        interviewee.add(p);
      }
    }
    if(intvwers != null) {
      for(Long r : intvwers) {
        Person p = new Person();
        p.setId(r);
        interviewer.add(p);
      }
    }
  }

}
