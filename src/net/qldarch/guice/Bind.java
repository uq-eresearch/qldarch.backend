package net.qldarch.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {
  boolean eagerSingleton() default false;
  Class<?>[] to() default None.class;
  Class<?> provides() default None.class;
}
