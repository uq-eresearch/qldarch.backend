package net.qldarch.gson.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class EnumSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o != null) {
      return new JsonPrimitive(o.toString());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
