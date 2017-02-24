package net.qldarch.gson.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class NullSerializer implements Serializer {
  @Override
  public JsonElement serialize(Object o, Context ctx) {
    return JsonNull.INSTANCE;
  }
}
