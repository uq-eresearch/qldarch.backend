package net.qldarch.text;

import java.sql.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;

@Entity
@Table(name = "text")
@Data
@EqualsAndHashCode(callSuper=true)
public class Article extends ArchObj {

  private String periodical;

  private String volume;

  private String issue;
  
  private Date published;

  private String pages;

  private String authors;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    m.put("periodical", periodical);
    m.put("volume", volume);
    m.put("issue", issue);
    m.put("published", published);
    m.put("pages", pages);
    m.put("authors", authors);
    return m;
  }

}
