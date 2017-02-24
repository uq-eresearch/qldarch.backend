package net.qldarch.gson;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

import com.google.gson.ExclusionStrategy;
import com.google.gson.GsonBuilder;

import net.qldarch.guice.Bind;

@Bind
public class AnnotatedExclusions {

  private Stream<JsonSkipField> skipFields(Annotation[] annotations) {
    return Stream.concat(Annotations.annotations(annotations, JsonSkipField.class),
        Annotations.annotations(annotations, JsonSkipFields.class).flatMap(jsonSkipFields ->
        Arrays.stream(jsonSkipFields.value())));
  }

  private Stream<ExclusionStrategy> exclusions(Annotation[] annotations) {
    return skipFields(annotations).map(jsonSkipField -> new FieldExclusionStrategy(
        jsonSkipField.type(), jsonSkipField.field()));
  }

  public void setup(GsonBuilder builder, Annotation[] annotations) {
    builder.setExclusionStrategies(Stream.concat(Stream.of(new AnnotatedFieldExclusionStrategy()),
        exclusions(annotations)).toArray(ExclusionStrategy[]::new));
  }

}
