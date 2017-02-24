package net.qldarch.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(JsonRegisterTypeAdapters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRegisterTypeAdapter {
  static final class None {}

  Class<?> type() default None.class;
  Class<?> adapter() default None.class;
}
