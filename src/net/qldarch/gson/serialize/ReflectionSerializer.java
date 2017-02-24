package net.qldarch.gson.serialize;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.qldarch.gson.JsonExclude;

public class ReflectionSerializer implements Serializer {

  private Map<Class<?>, Set<String>> excludes = new HashMap<>();

  private String getObjectKey(Field field) {
    // TODO add annotation check (SerializedName)
    return field.getName();
  }

  private boolean skip(Class<?> type, Field field) {
    final int modifiers = field.getModifiers();
    if(Modifier.isStatic(modifiers)) {
      return true;
    }
    if(Modifier.isTransient(modifiers)) {
      return true;
    }
    if(field.getAnnotation(JsonExclude.class) != null) {
      return true;
    }
    Set<String> s = excludes.get(type);
    if(s!=null) {
      if(s.contains(field.getName())) {
        return true;
      }
    }
    return false;
  }

  private Object value(Field field, Object o) {
    try {
      field.setAccessible(true);
      return field.get(o);
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to retrieve value from field '%s' of type '%s'",
          field.getName(), o.getClass().getName()));
    }
  }

  private JsonElement serialize(JsonObject jsonObject, Object o, Class<?> cls, Context ctx) {
    Class<?> superClass = cls.getSuperclass();
    if(superClass != null) {
      serialize(jsonObject, o, superClass, ctx);
    }
    Field[] fields = cls.getDeclaredFields();
    for(Field field : fields) {
      if(!skip(cls, field)) {
        final String key = getObjectKey(field);
        final Object value = value(field, o);
        jsonObject.add(key, ctx.serialize(value, Serializer.bracket(key)));
      }
    }
    return jsonObject;
  }

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o == null) {
      return JsonNull.INSTANCE;
    }
    return serialize(new JsonObject(), o, o.getClass(), ctx);
  }

  public void excludeField(Class<?> type, String fieldname) {
    Set<String> s = excludes.get(type);
    if(s == null) {
      s = new HashSet<>();
      excludes.put(type, s);
    }
    s.add(fieldname);
  }

}
