package net.qldarch.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class AnnotatedFieldExclusionStrategy implements ExclusionStrategy {

  @Override
  public boolean shouldSkipClass(Class<?> cls) {
    return false;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes fieldAttributes) {
    return (fieldAttributes.getAnnotation(JsonExclude.class) != null);
  }

}
