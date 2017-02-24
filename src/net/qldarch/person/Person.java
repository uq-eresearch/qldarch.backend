package net.qldarch.person;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.qldarch.archobj.ArchObj;
import net.qldarch.interview.Interview;
import net.qldarch.util.ObjUtils;

@Entity
@Table(name = "person")
@Data
@EqualsAndHashCode(callSuper=true, exclude={"interviews"})
@ToString
public class Person extends ArchObj {

  private static final String FIRST = "firstname";
  private static final String LAST = "lastname";
  private static final String PREFLABEL = "preflabel";
  private static final String PIQ = "practicedinqueensland";
  private static final String ARCHITECT = "architect";

  private String firstname;

  private String lastname;

  private String preflabel;

  private boolean practicedinqueensland;

  private boolean architect;

  @ManyToMany
  @JoinTable(
      name="interviewee",
      joinColumns=@JoinColumn(name="interviewee"),
      inverseJoinColumns=@JoinColumn(name="interview"))
  private Set<Interview> interviews;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    m.put(FIRST, firstname);
    m.put(LAST, lastname);
    m.put(PREFLABEL, preflabel);
    m.put(PIQ, practicedinqueensland);
    m.put(ARCHITECT, architect);
    return m;
  }

  @Override
  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = super.updateFrom(m);
    if(hasChanged(m, FIRST, firstname)) {
      changed = true;
      firstname = ObjUtils.asString(m.get(FIRST));
    }
    if(hasChanged(m, LAST, lastname)) {
      changed = true;
      lastname = ObjUtils.asString(m.get(LAST));
    }
    if(hasChanged(m, PREFLABEL, preflabel)) {
      changed = true;
      preflabel = ObjUtils.asString(m.get(PREFLABEL));
    }
    if(hasChanged(m, PIQ, ObjUtils::asBoolean, practicedinqueensland)) {
      final Boolean b =  ObjUtils.asBoolean(m.get(PIQ));
      if(b!=null) {
        changed = true;
        practicedinqueensland = b;
      }
    }
    if(hasChanged(m, ARCHITECT, ObjUtils::asBoolean, architect)) {
      final Boolean b = ObjUtils.asBoolean(m.get(ARCHITECT));
      if(b!=null) {
        changed = true;
        architect = b;
      }
    }
    return changed;
  }

  @Override
  public void copyFrom(Map<String, Object> m) {
    super.copyFrom(m);
    firstname = ObjUtils.asString(m.get(FIRST));
    lastname = ObjUtils.asString(m.get(LAST));
    preflabel = ObjUtils.asString(m.get(PREFLABEL));
    practicedinqueensland = ObjUtils.asBoolean(m.get(PIQ), true);
    architect = ObjUtils.asBoolean(m.get(ARCHITECT), true);
  }
}
