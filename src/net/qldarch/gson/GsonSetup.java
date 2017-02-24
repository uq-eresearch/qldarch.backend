package net.qldarch.gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;

@Bind
@Slf4j
public class GsonSetup {

  @Inject
  private AnnotatedExclusions exclusions;

  private GsonBuilder std(boolean serializeNulls, boolean excludeFieldsWithoutExposeAnnotation) {
    GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
    if(serializeNulls) {
      builder.serializeNulls();
    }
    if(excludeFieldsWithoutExposeAnnotation) {
      builder.excludeFieldsWithoutExposeAnnotation();
    }
    builder.registerTypeAdapter(Timestamp.class, new JsonSerializer<Timestamp>() {
      @Override
      public JsonElement serialize(Timestamp t, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(t));
      }});
    builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
      @Override
      public JsonElement serialize(Date d, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd").format(d));
      }});
    return builder;
  }

  private GsonBuilder builder(Annotation[] annotations) {
    final boolean serializeNulls = serializeNulls(annotations);
    final boolean excludeFieldsWithoutExposeAnnotation =
        excludeFieldsWithoutExposeAnnotation(annotations);
    final int fixedLengthDecimal = fixedLengthDecimal(annotations);
    final GsonBuilder builder = std(serializeNulls, excludeFieldsWithoutExposeAnnotation);
    if(fixedLengthDecimal >= 0) {
      builder.registerTypeAdapter(
          Double.class, new JsonSerializer<Double>() {
            @Override
            public JsonElement serialize(Double number, Type type, JsonSerializationContext ctx) {
              if(number != null) {
                BigDecimal value = BigDecimal.valueOf(number);
                return new JsonPrimitive(value.setScale(fixedLengthDecimal, BigDecimal.ROUND_HALF_UP));
              } else {
                return null;
              }
            }});
    }
    registerTypeAdapters(builder, annotations);
    exclusions.setup(builder, annotations);
    return builder;
  }

  private boolean serializeNulls(Annotation[] annotations) {
    return Annotations.annotations(annotations, JsonSerializeNulls.class).map(
        JsonSerializeNulls::value).findFirst().orElse(false);
  }

  private boolean excludeFieldsWithoutExposeAnnotation(Annotation[] annotations) {
    return Annotations.annotations(annotations, JsonExcludeFieldsWithoutExposeAnnotation.class).map(
        JsonExcludeFieldsWithoutExposeAnnotation::value).findFirst().orElse(false);
  }

  private int fixedLengthDecimal(Annotation[] annotations) {
    return Annotations.annotations(annotations, JsonFixedLengthDecimal.class).mapToInt(
        JsonFixedLengthDecimal::value).findFirst().orElse(-1);
  }

  private Stream<JsonRegisterTypeAdapter> registerTypeAdapters(Annotation[] annotations) {
    return Stream.concat(Annotations.annotations(annotations, JsonRegisterTypeAdapter.class),
        Annotations.annotations(annotations, JsonRegisterTypeAdapters.class).flatMap(j ->
        Arrays.stream(j.value())));
  }

  private void registerTypeAdapters(final GsonBuilder builder, final Annotation[] annotations) {
    registerTypeAdapters(annotations).forEach(rta -> {
      if((rta.type() != JsonRegisterTypeAdapter.None.class) &&
          (rta.adapter() != JsonRegisterTypeAdapter.None.class)) {
        try {
          builder.registerTypeAdapter(rta.type(), rta.adapter().newInstance());
        } catch(ReflectiveOperationException e) {
          log.warn("failed to register type adaptor {} for type {}",
              rta.adapter().getName(), rta.type().getName(), e);
        }
      }
    });
  }

  GsonBuilder setup(Annotation[] annotations) {
    return builder(annotations);
  }

}
