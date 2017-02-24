package net.qldarch.gson.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonNull;

public class StringSerializer  implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof String) {
      return new JsonPrimitive((String)o);
    } else if(o!= null) {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
