package net.qldarch.gson.serialize;

import java.util.Collection;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class CollectionSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof Collection) {
      Collection<?> collection = (Collection<?>)o;
      JsonArray jsonArray = new JsonArray();
      Iterator<?> iter = collection.iterator();
      int i = 0;
      while(iter.hasNext()) {
        jsonArray.add(ctx.serialize(iter.next(), Serializer.bracket(i++)));
      }
      return jsonArray;
    } else if(o != null) {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
