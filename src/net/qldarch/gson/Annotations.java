package net.qldarch.gson;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

import net.qldarch.util.StreamUtils;

public class Annotations {

  private static Stream<Annotation> annotations(Annotation[] annotations) {
    return annotations!=null?Arrays.stream(annotations):Stream.empty();
  }

  public static <T extends Annotation> Stream<T> annotations(Annotation[] annotations, Class<T> cls) {
    return StreamUtils.instancesOf(annotations(annotations), cls);
  }

}
