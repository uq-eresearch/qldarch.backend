package net.qldarch.archobj;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import net.qldarch.gson.serialize.Context;
import net.qldarch.gson.serialize.Serializer;
import net.qldarch.media.Media;
import net.qldarch.person.Person;

public class SimpleArchObjSerializer implements Serializer {

  private Long preferredImageId(ArchObj o) {
    final Media preferred = Media.preferredImage(o.getMedia());
    return preferred!=null?preferred.getId():null;
  }

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof ArchObj) {
      ArchObj ao = (ArchObj)o;
      if(ao.isDeleted()) {
        return JsonNull.INSTANCE;
      } else {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", ao.getId());
        jsonObject.addProperty("label", ao.getLabel());
        jsonObject.addProperty("media", preferredImageId(ao));
        if(ao.getType() != null) {
          jsonObject.addProperty("type", ao.getType().toString());
        }
        if(ao instanceof Person) {
          jsonObject.addProperty("architect", ((Person) ao).isArchitect());
        }
        return jsonObject;
      }
    } else {
      return ctx.delegate(o);
    }
  }

}
