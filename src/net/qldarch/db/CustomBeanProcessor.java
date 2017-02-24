package net.qldarch.db;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.lang3.StringUtils;

class CustomBeanProcessor extends BeanProcessor {

  private Map<String, ColumnConverter<?>> converters;

  CustomBeanProcessor(Map<String, ColumnConverter<?>> converters, Map<String, String> columnToPropertyOverrides) {
    super(columnToPropertyOverrides);
    this.converters = converters;
  }

  @Override
  protected Object processColumn(ResultSet rs, int index, Class<?> propType)
      throws SQLException {
    ColumnConverter<?> cc = converters.get(rs.getMetaData().getColumnLabel(index));
    return cc!=null?cc.convert(rs, index):super.processColumn(rs, index, propType);
  }

  static CustomBeanProcessor setup(Class<?> type, Map<String, ColumnConverter<?>> converters) {
    Map<String, String> columnToPropertyOverrides = new HashMap<>();
    Map<String, ColumnConverter<?>> combined = new HashMap<>();
    // TODO This only works for the fields declared on the type but not for inherited fields
    for(Field field : type.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);
      if(column != null) {
        String name = column.name();
        if(StringUtils.isNotBlank(name)) {
          columnToPropertyOverrides.put(name, field.getName());
        } else {
          name = field.getName();
        }
        if(column.converter() != DefaultConverter.class) {
          try {
            combined.put(name, column.converter().newInstance());
          } catch(Exception e) {
            throw new RuntimeException(String.format(
                "failed to create converter instance for column %s", name), e);
          }
        }
      }
    }
    if(converters != null) {
      combined.putAll(converters);
    }
    return new CustomBeanProcessor(combined, columnToPropertyOverrides);
  }

}
