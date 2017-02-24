package net.qldarch.util;

import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {

  /**
   * When the returned {@code Function} is passed as an argument to
   * {@link Stream#flatMap}, the result is a stream of instances of
   * {@code cls}.
   */
  // http://stackoverflow.com/a/24191705
  public static <E> Function<Object, Stream<E>> instancesOf(Class<E> cls) {
    return o -> cls.isInstance(o)?Stream.of(cls.cast(o)):Stream.empty();
  }

  public static <T,R> Stream<R> instancesOf(Stream<T> stream, Class<R> cls) {
    return stream.flatMap(instancesOf(cls));
  }

}
