package net.qldarch.gson.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class NumberSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof Number) {
      return new JsonPrimitive((Number)o);
    } else if(o != null) {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
