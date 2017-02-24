package net.qldarch.gson.serialize;

import java.lang.reflect.Array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class ArraySerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o == null) {
      return JsonNull.INSTANCE;
    } else if (o.getClass().isArray()) {
      int length = Array.getLength(o);
      JsonArray jsonArray = new JsonArray();
      for (int i = 0; i < length; i ++) {
        jsonArray.add(ctx.serialize(Array.get(o, i), Serializer.bracket(i)));
      }
      return jsonArray;
    } else {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    }
  }

}
