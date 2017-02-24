package net.qldarch.firm;

import java.sql.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;

@Entity
@Table(name = "firm")
@Data
@EqualsAndHashCode(callSuper=true)
public class Firm extends ArchObj {

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
    m.put("australian", australian);
    if(start != null) {
      m.put("start", start);
    }
    if(end != null) {
      m.put("end", end);
    }
    return m;
  }

}
