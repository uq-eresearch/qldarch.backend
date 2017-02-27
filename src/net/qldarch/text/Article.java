package net.qldarch.text;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.sql.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;
import net.qldarch.util.DateUtil;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Entity
@Table(name = "text")
@Data
@EqualsAndHashCode(callSuper=true)
public class Article extends ArchObj {

  private static final String PERIODICAL = "periodical";
  private static final String VOLUME = "volume";
  private static final String ISSUE = "issue";
  private static final String PUBLISHED = "published";
  private static final String PAGES = "pages";
  private static final String AUTHORS = "authors";

  private String periodical;

  private String volume;

  private String issue;
  
  private Date published;

  private String pages;

  private String authors;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    m.put(PERIODICAL, periodical);
    m.put(VOLUME, volume);
    m.put(ISSUE, issue);
    m.put(PUBLISHED, published);
    m.put(PAGES, pages);
    m.put(AUTHORS, authors);
    return m;
  }

  @Override
  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = super.updateFrom(m);
    if(UpdateUtils.hasChanged(m, PERIODICAL, periodical)) {
      changed = true;
      periodical = ObjUtils.asString(m.get(PERIODICAL));
    }
    if(UpdateUtils.hasChanged(m, VOLUME, volume)) {
      changed = true;
      volume = ObjUtils.asString(m.get(VOLUME));
    }
    if(UpdateUtils.hasChanged(m, ISSUE, issue)) {
      changed = true;
      issue = ObjUtils.asString(m.get(ISSUE));
    }
    if(hasChanged(m, PUBLISHED, o-> DateUtil.toSqlDate(
        ObjUtils.asDate(o, DateUtil.YYYY_MM_DD)), published)) {
      changed = true;
      published = DateUtil.toSqlDate(ObjUtils.asDate(m.get(PUBLISHED), "yyyy-MM-dd"));
    }
    if(UpdateUtils.hasChanged(m, PAGES, pages)) {
      changed = true;
      pages = ObjUtils.asString(m.get(PAGES));
    }
    if(UpdateUtils.hasChanged(m, AUTHORS, authors)) {
      changed = true;
      authors = ObjUtils.asString(m.get(AUTHORS));
    }
    return changed;
  }

  @Override
  public void copyFrom(Map<String, Object> m) {
    super.copyFrom(m);
    this.periodical = ObjUtils.asString(m.get(PERIODICAL));
    this.volume = ObjUtils.asString(m.get(VOLUME));
    this.issue = ObjUtils.asString(m.get(ISSUE));
    this.published = DateUtil.toSqlDate(ObjUtils.asDate(m.get(PUBLISHED), DateUtil.YYYY_MM_DD));
    this.pages = ObjUtils.asString(m.get(PAGES));
    this.authors = ObjUtils.asString(m.get(AUTHORS));
  }

}
