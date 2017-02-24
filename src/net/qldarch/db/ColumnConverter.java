package net.qldarch.db;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ColumnConverter<T> {
  public T convert(ResultSet rs, int i) throws SQLException;
}
