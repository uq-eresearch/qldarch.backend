package net.qldarch.gson.serialize;

import net.qldarch.jsonpath.JsonPath;

public interface SerializerRegistration {
  Serializer getSerializer();
  boolean canSerialize(Class<?> cls, JsonPath path);
}
