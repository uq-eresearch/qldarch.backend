package net.qldarch.gson.serialize;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import net.qldarch.gson.Annotations;
import net.qldarch.gson.JsonSkipField;
import net.qldarch.gson.JsonSkipFields;
import net.qldarch.jsonpath.JsonPath;

public class JsonSetup {

  private Stream<JsonSkipField> skipFields(Annotation[] annotations) {
    return Stream.concat(Annotations.annotations(annotations, JsonSkipField.class),
        Annotations.annotations(annotations, JsonSkipFields.class).flatMap(jsonSkipFields ->
        Arrays.stream(jsonSkipFields.value())));
  }

  private Stream<JsonSerializer> serializers(Annotation[] annotations) {
    return Stream.concat(Annotations.annotations(annotations, JsonSerializer.class),
        Annotations.annotations(annotations, JsonSerializers.class).flatMap(j ->
        Arrays.stream(j.value())));
  }

  private Serializer newInstance(Class<? extends Serializer> cls) {
    try {
      return cls.newInstance();
    } catch(Exception e) {
      throw new RuntimeException("failed to create new serializer instance of " + cls.getName());
    }
  }

  private SerializerRegistration createSerializerRegistration(JsonSerializer a) {
    final JsonPath path = StringUtils.isNotBlank(a.path())?JsonPath.parse(a.path()):null;
    final Class<?> type = a.type();
    return new StdSerializerRegistration(cls -> type.isAssignableFrom(cls),
        path, newInstance(a.serializer()));
  }

  public Json setup(Annotation[] annotations) {
    try {
      List<SerializerRegistration> serializers = serializers(annotations).map(
          this::createSerializerRegistration).collect(Collectors.toList());
      final Json json = new Json(serializers);
      skipFields(annotations).forEach(field -> json.excludeField(field.type(), field.field()));
      return json;
    } catch(Exception e) {
      throw new RuntimeException("failed to create json serializer", e);
    }
  }

}
