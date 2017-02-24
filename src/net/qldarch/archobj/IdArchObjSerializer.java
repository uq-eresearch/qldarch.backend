package net.qldarch.archobj;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import net.qldarch.gson.serialize.Context;
import net.qldarch.gson.serialize.Serializer;

public class IdArchObjSerializer implements Serializer {

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof ArchObj) {
      if(((ArchObj)o).isDeleted()) {
        return JsonNull.INSTANCE;
      } else {
        return ctx.serialize(((ArchObj)o).getId());
      }
    } else {
      return ctx.delegate(o);
    }
  }

}
