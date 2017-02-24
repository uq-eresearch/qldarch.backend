package net.qldarch.jaxrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// Repeating Annotations are explained here:
// https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
@Repeatable(ResponseHeaders.class)
public @interface ResponseHeader {
  String value();
}
