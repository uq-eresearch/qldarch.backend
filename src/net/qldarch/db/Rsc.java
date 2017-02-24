package net.qldarch.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import net.qldarch.util.M;

public class Rsc {

  public static List<Map<String, Object>> fetchAll(ResultSet rs) throws SQLException {
    return new MapListHandler(new CustomRowProcessor()).handle(rs);
  }

  public static Map<String, Object> fetchFirst(ResultSet rs) throws SQLException {
    return new MapHandler(new CustomRowProcessor()).handle(rs);
  }

  public static <T> ResultSetConsumer<T> bean(final Class<T> type) {
    return bean(type, null);
  }

  public static <T> ResultSetConsumer<T> bean(final Class<T> type,
      Map<String, ColumnConverter<?>> converters) {
    return rs -> new BeanHandler<T>(type, new BasicRowProcessor(
        CustomBeanProcessor.setup(type, converters))).handle(rs);
  }
  

  public static <T> ResultSetConsumer<List<T>> beans(final Class<T> type) {
    return rs -> new BeanListHandler<T>(type).handle(rs);
  }


  /**
   * helpful if the query returns a single number like e.g. select count(x) from ... 
   */
  public static long singleIntegral(ResultSet rs) throws SQLException {
    Object o = M.singleValue(fetchFirst(rs));
    if(o instanceof Number) {
      return ((Number) o).longValue();
    } else {
      throw new RuntimeException(String.format("expected number (%s)", o));
    }
  }

}
