package net.qldarch.db;

import java.sql.ResultSet;
import java.sql.SQLException;

// This converter is used on the column annotation as default 
// but should never actually be used anywhere
class DefaultConverter implements ColumnConverter<Object> {
  @Override
  public Object convert(ResultSet rs, int i) throws SQLException {
    throw new RuntimeException();
  }
}
