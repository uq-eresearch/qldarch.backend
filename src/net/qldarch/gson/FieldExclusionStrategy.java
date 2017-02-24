package net.qldarch.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FieldExclusionStrategy implements ExclusionStrategy {

  private Class<?> cls;

  private String fieldname;

  @Override
  public boolean shouldSkipClass(Class<?> cls) {
    return false;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes fieldAttribute) {
    return (cls.equals(fieldAttribute.getDeclaringClass())) && (fieldname.equals(fieldAttribute.getName())); 
  }

}
