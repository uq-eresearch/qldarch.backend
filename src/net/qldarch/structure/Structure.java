package net.qldarch.structure;

import static net.qldarch.util.UpdateUtils.hasChanged;

import java.sql.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import com.google.common.math.DoubleMath;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.qldarch.archobj.ArchObj;
import net.qldarch.util.DateUtil;
import net.qldarch.util.ObjUtils;
import net.qldarch.util.UpdateUtils;

@Entity
@Table(name="structure")
@Data
@EqualsAndHashCode(callSuper=true)
public class Structure extends ArchObj {

  private static final String LOCATION = "location";
  private static final String COMPLETION = "completion";
  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";
  private static final String AUSTRALIAN = "australian";
  private static final String DEMOLISHED = "demolished";
  private static final String TYPOLOGIES = "typologies";

  private String location;

  private Date completion;

  @Column(name="lat")
  private Double latitude;

  @Column(name="lng")
  private Double longitude;

  private boolean australian;

  private boolean demolished;

  @ElementCollection
  @CollectionTable(name="buildingtypology", joinColumns=@JoinColumn(name="structure"))
  @Column(name="typology")
  private Set<String> typologies;

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> m = super.asMap();
    if(StringUtils.isNotBlank(location)) {
      m.put(LOCATION, location);
    }
    if(completion != null) {
      m.put(COMPLETION, completion);
    }
    if(latitude != null) {
      m.put(LATITUDE, latitude);
    }
    if(longitude != null) {
      m.put(LONGITUDE, longitude);
    }
    m.put(AUSTRALIAN, australian);
    m.put(DEMOLISHED, demolished);
    m.put(TYPOLOGIES, typologies);
    return m;
  }

  @Override
  public boolean updateFrom(Map<String, Object> m) {
    boolean changed = super.updateFrom(m);
    if(UpdateUtils.hasChanged(m, LOCATION, location)) {
      changed = true;
      location = ObjUtils.asString(m.get(LOCATION));
    }
    if(hasChanged(m, COMPLETION, o-> DateUtil.toSqlDate(
        ObjUtils.asDate(o, "yyyy-MM-dd")), completion)) {
      changed = true;
      completion = DateUtil.toSqlDate(ObjUtils.asDate(m.get(COMPLETION), "yyyy-MM-dd"));
    }
    
// FIXME if the key is not in the map ignore other set to null if not already null. also latitude/longitude can be already null and the code below throws NPE in this case!
//    if((m.get(LATITUDE) != null) && !DoubleMath.fuzzyEquals(
//        ObjUtils.asDouble(m.get(LATITUDE)), latitude, 0.00001)) {
//      changed = true;
//      latitude = ObjUtils.asDouble(m.get(LATITUDE));
//    }
//    if((m.get(LONGITUDE) != null) && !DoubleMath.fuzzyEquals(
//        ObjUtils.asDouble(m.get(LONGITUDE)), longitude, 0.00001)) {
//      changed = true;
//      longitude = ObjUtils.asDouble(m.get(LONGITUDE));
//    }
    if(hasChanged(m, AUSTRALIAN, ObjUtils::asBoolean, australian)) {
      final Boolean b = ObjUtils.asBoolean(m.get(AUSTRALIAN));
      if(b!=null) {
        changed = true;
        australian = b;
      }
    }
    if(hasChanged(m, DEMOLISHED, ObjUtils::asBoolean, demolished)) {
      final Boolean b = ObjUtils.asBoolean(m.get(DEMOLISHED));
      if(b!=null) {
        changed = true;
        demolished = b;
      }
    }
    // FIXME foreign key is missing buildingtypology for column typology references buildingtypologytype(type)
    if(hasChanged(m, TYPOLOGIES, o->ObjUtils.asSet(o), typologies)) {
      changed = true;
      typologies = (Set<String>)ObjUtils.asStringSet(m.get(TYPOLOGIES));
    }
    return changed;
  }

  @Override
  public void copyFrom(Map<String, Object> m) {
    super.copyFrom(m);
    location = ObjUtils.asString(m.get(LOCATION));
    completion = DateUtil.toSqlDate(DateUtil.guess(ObjUtils.asString(m.get(COMPLETION))));
    latitude = ObjUtils.asDouble(m.get(LATITUDE));
    longitude = ObjUtils.asDouble(m.get(LONGITUDE));
    australian = ObjUtils.asBoolean(m.get(AUSTRALIAN), true);
    demolished = ObjUtils.asBoolean(m.get(DEMOLISHED), false);
    typologies = (Set<String>)ObjUtils.asStringSet(m.get(TYPOLOGIES));
  }

}
