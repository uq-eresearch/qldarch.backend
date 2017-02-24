package net.qldarch.gson.serialize;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import net.qldarch.jsonpath.JsonPath;

public class Json {

  private final List<SerializerRegistration> serializers = new ArrayList<>();

  private final ReflectionSerializer reflectionSerializer = new ReflectionSerializer();

  public Json(List<SerializerRegistration> customSerializers) {
    if(customSerializers!=null) {
      serializers.addAll(customSerializers);
    }
    serializers.add(new AssignableTypeRegistration(String.class, new StringSerializer()));
    serializers.add(new AssignableTypeRegistration(Number.class, new NumberSerializer()));
    serializers.add(new AssignableTypeRegistration(Boolean.class, new BooleanSerializer()));
    serializers.add(new AssignableTypeRegistration(java.sql.Date.class, new DateSerializer("yyyy-MM-dd")));
    serializers.add(new AssignableTypeRegistration(Timestamp.class, new DateSerializer("yyyy-MM-dd HH:mm:ss.SSS")));
    serializers.add(new AssignableTypeRegistration(Time.class, new DateSerializer("HH:mm:ss")));
    serializers.add(new AssignableTypeRegistration(Date.class, new DateSerializer("yyyy-MM-dd HH:mm:ss")));
    serializers.add(new AssignableTypeRegistration(Collection.class, new CollectionSerializer()));
    serializers.add(new AssignableTypeRegistration(Map.class, new MapSerializer()));
    serializers.add(new CustomTypeRegistration(Class::isArray, new ArraySerializer()));
    serializers.add(new CustomTypeRegistration(Class::isEnum, new EnumSerializer()));
    serializers.add(new AssignableTypeRegistration(Object.class, reflectionSerializer));
  }

  public Json() {
    this(null);
  }

  public JsonElement toJsonTree(Object src) {
    return toJsonTree(src, "$", new Context(null, null, this, new IdentityHashMap<>()));
  }

  private List<Serializer> serializers(final Class<?> type, final JsonPath path) {
    return serializers.stream().filter(serializer -> serializer.canSerialize(type, path)).map(
        SerializerRegistration::getSerializer).collect(Collectors.toList());
  }

  public JsonElement toJsonTree(Object current, String path, Context ctx) {
//    System.out.println(path);
    if(current == null) {
      return JsonNull.INSTANCE;
    }
    if(ctx.visited().containsKey(current)) {
//      System.out.println("cycle in object graph detected, setting null");
      return JsonNull.INSTANCE;
    }
    IdentityHashMap<Object, Object> visited = new IdentityHashMap<>(ctx.visited());
    visited.put(current, current);
    List<Serializer> s = serializers(current.getClass(), JsonPath.parse(path));
    if(s.isEmpty()) {
      throw new RuntimeException("can't serialize type " + current.getClass().getName());
    }
    return s.get(0).serialize(current, new Context(path, Utils.tail(s), this, visited));
  }

  public void excludeField(Class<?> type, String fieldname) {
    reflectionSerializer.excludeField(type, fieldname);
  }

}
