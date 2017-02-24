package net.qldarch.db;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;

public class CustomRowProcessor extends BasicRowProcessor {

  @Override
  public Map<String, Object> toMap(ResultSet rs) throws SQLException {
    Map<String, Object> m = super.toMap(rs);
    for(Map.Entry<String, Object> me : m.entrySet()) {
      if(me.getValue() instanceof Array) {
        me.setValue(((Array)me.getValue()).getArray());
      }
    }
    return m;
  }

}
