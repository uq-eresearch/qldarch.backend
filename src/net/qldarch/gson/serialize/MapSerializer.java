package net.qldarch.gson.serialize;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class MapSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof Map) {
      Map<?,?> m = (Map<?,?>)o;
      final JsonObject jsonObject = new JsonObject();
      for(Map.Entry<?,?> me : m.entrySet()) {
        final Object key = me.getKey();
        if(key == null) {
          continue;
        }
        final Object value = me.getValue();
        // TODO key needs to be escaped for the json path (does it?)
        jsonObject.add(key.toString(), ctx.serialize(value, Serializer.bracket(key.toString())));
      }
      return jsonObject;
    } else if(o != null) {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
