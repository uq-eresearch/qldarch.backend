package net.qldarch.gson.serialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(JsonSerializers.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerializer {
  Class<?> type() default Object.class;
  String path() default "";
  Class<? extends Serializer> serializer() default NullSerializer.class;
}
