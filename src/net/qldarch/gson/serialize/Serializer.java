package net.qldarch.gson.serialize;

import com.google.gson.JsonElement;

@FunctionalInterface
public interface Serializer {
  JsonElement serialize(Object o, Context ctx);
  
  // TODO move bracket functions into a json path lib
  public static String bracket(String path) {
    return String.format("['%s']", path);
  }

  public static String bracket(int index) {
    return String.format("[%s]", index);
  }
}
