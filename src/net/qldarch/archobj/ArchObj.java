package net.qldarch.archobj;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Where;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.qldarch.gson.JsonExclude;
import net.qldarch.media.Media;
import net.qldarch.security.Updatable;
import net.qldarch.security.User;
import net.qldarch.util.ObjUtils;;

@Entity
@Table(name="archobj")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(of={"id"})
@ToString
public class ArchObj implements Updatable {

  private static final String LABEL = "label";
  private static final String SUMMARY = "summary";
  private static final String NOTE = "note";

  @Id
  private Long id;

  private Long version;

  private String label;

  private String summary;

  private String note;

  @Enumerated(EnumType.STRING)
  private ArchObjType type;

  private Date created;

  private Timestamp locked;

  @JsonExclude
  private Timestamp deleted;

  private Long owner;

  @OneToMany(mappedBy="depicts")
  @Where(clause="deleted is null")
  private Set<Media> media;

  @Transient
  private List<Map<String, Object>> relationships;

  @Transient
  private List<Map<String, Object>> associatedMedia;

  public Map<String, Object> asMap() {
    // only add relevant fields here, used for search and versioning
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id);
    m.put(LABEL, label);
    if(StringUtils.isNotBlank(summary)) {
      m.put(SUMMARY, summary);
    }
    if(StringUtils.isNotBlank(note)) {
      m.put(NOTE, note);
    }
    m.put("type", type.toString());
    if(created != null) {
      m.put("created", created);
    }
    return m;
  }

  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = false;
    if(hasChanged(m, LABEL, label)) {
      final String newLabel = ObjUtils.asString(m.get(LABEL));
      if(StringUtils.isNotBlank(newLabel)) {
        changed = true;
        label = newLabel;
      }
    }
    if(hasChanged(m, SUMMARY, summary)) {
      changed = true;
      summary = ObjUtils.asString(m.get(SUMMARY));
    }
    if(hasChanged(m, NOTE, note)) {
      changed = true;
      note = ObjUtils.asString(m.get(NOTE));
    }
    return changed;
  }

  public void copyFrom(Map<String, Object> m) {
    label = ObjUtils.asString(m.get(LABEL));
    summary = ObjUtils.asString(m.get(SUMMARY));
    note = ObjUtils.asString(m.get(NOTE));
  }

  public boolean isDeleted() {
    return deleted != null;
  }

  @Override
  public boolean canUpdate(User user) {
    return (user != null) && (user.isAdmin() ||(user.isEditor() && (getLocked() == null)));
  }

}
