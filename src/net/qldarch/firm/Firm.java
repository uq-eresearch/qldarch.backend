package net.qldarch.firm;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.qldarch.archobj.ArchObj;
import net.qldarch.guice.Guice;
import net.qldarch.hibernate.HS;
import net.qldarch.relationship.Relationship;
import net.qldarch.relationship.RelationshipSource;
import net.qldarch.relationship.RelationshipType;
import net.qldarch.security.User;
import net.qldarch.util.DateUtil;
import net.qldarch.util.ObjUtils;

@Entity
@Table(name = "firm")
@Data
@EqualsAndHashCode(callSuper=true)
@Slf4j
public class Firm extends ArchObj {

  private static final String AUSTRALIAN = "australian";
  private static final String START = "start";
  private static final String END = "end";
  private static final String PRECEDEDBY = "precededby";
  private static final String SUCCEEDEDBY = "succeededby";
  private static final String EMPLOYEES = "employees";

  private boolean australian;

  @Column(name="startdate")
  private Date start;

  @Column(name="enddate")
  private Date end;

  @OneToOne
  @JoinColumn(name = "precededby")
  private Firm precededby;

  @OneToOne
  @JoinColumn(name = "succeededby")
  private Firm succeededby;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    m.put(AUSTRALIAN, australian);
    if(start != null) {
      m.put(START, start);
    }
    if(end != null) {
      m.put(END, end);
    }
    if(precededby != null) {
      m.put(PRECEDEDBY, precededby.getId());
    }
    if(succeededby != null) {
      m.put(SUCCEEDEDBY, succeededby.getId());
    }
    return m;
  }

  @Override
  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = super.updateFrom(m);
    if(hasChanged(m, AUSTRALIAN, ObjUtils::asBoolean, australian)) {
      final Boolean b = ObjUtils.asBoolean(m.get(AUSTRALIAN));
      if(b!=null) {
        changed = true;
        australian = b;
      }
    }
    if(hasChanged(m, START, o-> DateUtil.toSqlDate(
        ObjUtils.asDate(o, DateUtil.YYYY_MM_DD)), start)) {
      changed = true;
      start = DateUtil.toSqlDate(ObjUtils.asDate(m.get(START), "yyyy-MM-dd"));
    }
    if(hasChanged(m, END, o-> DateUtil.toSqlDate(
        ObjUtils.asDate(o, DateUtil.YYYY_MM_DD)), end)) {
      changed = true;
      end = DateUtil.toSqlDate(ObjUtils.asDate(m.get(END), "yyyy-MM-dd"));
    }
    if(hasChanged(m, PRECEDEDBY, ObjUtils::asLong, precededby!=null?precededby.getId():null)) {
      changed = true;
      Long id = ObjUtils.asLong(m.get(PRECEDEDBY));
      precededby = id!=null?loadFirm(id):null;
    }
    if(hasChanged(m, SUCCEEDEDBY, ObjUtils::asLong, succeededby!=null?succeededby.getId():null)) {
      changed = true;
      Long id = ObjUtils.asLong(m.get(SUCCEEDEDBY));
      succeededby = id!=null?loadFirm(id):null;
    }
    return changed;
  }

  private Firm loadFirm(Long id) {
    try {
      if(id != null) {
        return Guice.injector().getInstance(HS.class).get(Firm.class, id);
      }
      return null;
    } catch(Exception e) {
      log.debug("while loading firm with id '{}'", id, e);
    }
    return null;
  }

  @Override
  public void copyFrom(Map<String, Object> m) {
    super.copyFrom(m);
    australian = ObjUtils.asBoolean(m.get(AUSTRALIAN), true);
    start = DateUtil.toSqlDate(ObjUtils.asDate(m.get(START), DateUtil.YYYY_MM_DD));
    end = DateUtil.toSqlDate(ObjUtils.asDate(m.get(END), DateUtil.YYYY_MM_DD));
    precededby = loadFirm(ObjUtils.asLong(m.get(PRECEDEDBY)));
    succeededby = loadFirm(ObjUtils.asLong(m.get(SUCCEEDEDBY)));
  }

  private void addEmploymentRelationship(Long employeeId) {
    if(employeeId != null) {
      final HS hs = Guice.injector().getInstance(HS.class);
      final User user = Guice.injector().getInstance(User.class);
      Relationship r = new Relationship();
      r.setObject(this.getId());
      r.setSubject(employeeId);
      r.setType(RelationshipType.Employment);
      r.setSource(RelationshipSource.firm);
      r.setOwner(user.getId());
      r.setCreated(new Timestamp(System.currentTimeMillis()));
      hs.save(r);
    }
  }

  @Override
  protected void postCreate(Map<String, Object> m) {
    final Object o = m.get(EMPLOYEES);
    if(o instanceof String) {
      addEmploymentRelationship(ObjUtils.asLong(o));
    } else if(o instanceof List) {
      final List<?> l = (List<?>)o;
      l.forEach(eId -> addEmploymentRelationship(ObjUtils.asLong(eId)));
    }
  }
}
