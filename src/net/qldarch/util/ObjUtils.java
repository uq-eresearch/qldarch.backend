package net.qldarch.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjUtils {

  public static String asString(Object value) {
    if(value instanceof String) {
      return (String)value;
    } else if(value != null) {
      return value.toString();
    } else {
      return null;
    }
  }

  public static Boolean asBoolean(Object value) {
    if(value instanceof Boolean) {
      return (Boolean)value;
    } else if(value instanceof String) {
      return Boolean.valueOf((String)value);
    } else if(value != null) {
      return Boolean.valueOf(value.toString());
    } else {
      return null;
    }
  }

  public static boolean asBoolean(Object value, boolean defaultValue) {
    final Boolean b = asBoolean(value);
    return b!=null?b:defaultValue;
  }

  public static Long asLong(Object o) {
    if(o == null) {
      return null;
    } else if(o instanceof Number) {
      return ((Number) o).longValue();
    } else if(o instanceof String) {
      try {
        return new Long((String)o);
      } catch(Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }

  public static Date asDate(Object o, String format) {
    if(o instanceof Date) {
      return (Date)o;
    } else if(o instanceof String) {
      return DateUtil.parseDate((String)o, format);
    } else {
      return null;
    }
  }

  public static Double asDouble(Object value) {
    if(value instanceof Double) {
      return (Double)value;
    } else if(value instanceof Number) {
      return ((Number)value).doubleValue();
    } else if(value instanceof String) {
      try {
        return new Double((String)value);
      } catch(Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }

  public static List<String> asStringList(Object o) {
    if(o instanceof Collection) {
      return ((Collection<?>)o).stream().map(ObjUtils::asString).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public static Set<?> asSet(Object o) {
    if(o instanceof Set) {
      return (Set<?>)o;
    } else if(o instanceof Collection) {
      return ((Collection<?>)o).stream().collect(Collectors.toSet());
    } else {
      return Collections.singleton(o);
    }
  }

  public static Set<String> asStringSet(Object o) {
    if(o instanceof Collection) {
      return ((Collection<?>)o).stream().map(ObjUtils::asString).collect(Collectors.toSet());
    } else if(o != null) {
      return Collections.singleton(ObjUtils.asString(o));
    } else {
      return null;
    }
  }

  public static Integer asInteger(Object o) {
    if(o == null) {
      return null;
    } else if(o instanceof Number) {
      return ((Number) o).intValue();
    } else if(o instanceof String) {
      try {
        return new Integer((String) o);
      } catch(Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }
}
