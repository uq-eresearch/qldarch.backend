package net.qldarch.gson.serialize;

@FunctionalInterface
public interface ClassSerializable {
  boolean canSerialize(Class<?> cls);
}
