package net.qldarch.db;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

public class SVal {

  private static class GenericSqlValue implements SqlValue {

    private final Object val;
    private final int type;

    GenericSqlValue(Object val, int type) {
      this.val = val;
      this.type = type;
    }

    @Override
    public Object value() {
      return val;
    }

    @Override
    public int type() {
      return type;
    }

    @Override
    public String toString() {
      return val!=null?val.toString():null;
    }
  }

  public static SqlValue of(String s) {
    return new GenericSqlValue(s, Types.VARCHAR);
  }

  public static SqlValue of(List<?> list) {
    return new GenericSqlValue(list, Types.ARRAY);
  }

  public static SqlValue of(Integer i) {
    return new GenericSqlValue(i, Types.INTEGER);
  }

  public static SqlValue of(Date d) {
    return new GenericSqlValue(d, Types.DATE);
  }

  public static SqlValue of(Timestamp t) {
    return new GenericSqlValue(t, Types.TIMESTAMP);
  }

  public static SqlValue of(Double d) {
    return new GenericSqlValue(d, Types.REAL);
  }

  public static SqlValue of(Long l) {
    return new GenericSqlValue(l, Types.BIGINT);
  }

  public static SqlValue of(Boolean b) {
    return new GenericSqlValue(b, Types.BOOLEAN);
  }

  public static SqlValue nullVal(int type) {
    return new GenericSqlValue(null, type);
  }

  public static SqlValue ofUnknownType(Object o) {
    if(o == null) {
      throw new RuntimeException("can not convert null into sql value");
    } else if(o instanceof SqlValue) {
      return (SqlValue)o;
    } else if(o instanceof String) {
      return of((String)o);
    } else if(o instanceof List) {
      return of((List<?>)o);
    } else if(o instanceof Integer) {
      return of((Integer)o);
    } else if(o instanceof Timestamp) {
      return of((Timestamp)o);
    } else if(o instanceof Date) {
      return of((Date)o);
    } else if(o instanceof Double) {
      return of((Double)o);
    } else if(o instanceof Long) {
      return of((Long)o);
    } else if(o instanceof Boolean) {
      return of((Boolean)o);
    } else {
      throw new RuntimeException(String.format(
          "can not convert into sql value, type %s not supported", o.getClass().getName()));
    }
  }

}
