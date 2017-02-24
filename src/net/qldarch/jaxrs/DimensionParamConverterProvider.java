package net.qldarch.jaxrs;

import java.awt.Dimension;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

@Provider
public class DimensionParamConverterProvider implements ParamConverterProvider {

  private Dimension parse(String dimension) {
    try {
      String[] d0 = StringUtils.split(dimension, 'x');
      return new Dimension(Integer.parseInt(d0[0]), Integer.parseInt(d0[1]));
    } catch(Exception e) {
      return null;
    }
  }

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> cls, Type type, Annotation[] annotation) {
    if(cls.equals(Dimension.class)) {
      return new ParamConverter<T>() {

        @Override
        public T fromString(String s) {
          return cls.cast(parse(s));
        }

        @Override
        public String toString(T o) {
          Dimension d = (Dimension)o;
          return String.format("%sx%s", d.width, d.height);
        }};
    } else {
      return null;
    }
  }

}
