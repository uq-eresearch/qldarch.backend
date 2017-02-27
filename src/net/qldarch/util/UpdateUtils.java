package net.qldarch.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.MultivaluedMap;

import com.google.common.math.DoubleMath;

public class UpdateUtils {

  @FunctionalInterface
  public static interface Converter {
    Object convert(Object o);
  }

  public static boolean hasChanged(Map<String, Object> m, String key, Object current) {
    return hasChanged(m, key, o->o, current);
  }

  public static boolean hasChanged(Map<String, Object> m, String key, Converter converter, Object current) {
    return m.containsKey(key) && !Objects.equals(current, converter.convert(m.get(key)));
  }

  public static boolean hasChangedDouble(Map<String, Object> m, String key, Double current) {
    if(m.containsKey(key)) {
      final Object v = m.get(key);
      if(v != null) {
        final Double d = ObjUtils.asDouble(v);
        if((current == null) && (d == null)) {
          return false;
        } else if((current != null) && (d != null)) {
          return !DoubleMath.fuzzyEquals(current, d, 0.00001);
        } else {
          return true;
        }
      }
    }
    return false;
  }

  public static Map<String, Object> asMap(MultivaluedMap<String, Object> multimap) {
    Map<String, Object> m = new HashMap<>();
    for(Map.Entry<String, List<Object>> me : multimap.entrySet()) {
      List<Object> l = me.getValue();
      if((l == null) || (l.size() == 0)) {
        m.put(me.getKey(), null);
      } else if(l.size() == 1) {
        m.put(me.getKey(), l.get(0));
      } else {
        m.put(me.getKey(), l);
      }
    }
    return m;
  }
}
