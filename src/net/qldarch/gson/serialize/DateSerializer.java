package net.qldarch.gson.serialize;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class DateSerializer implements Serializer {

  private final SimpleDateFormat formatter;

  public DateSerializer(String format) {
    this.formatter = new SimpleDateFormat(format);
  }

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    if(o instanceof Date) {
      return new JsonPrimitive(formatter.format((Date)o));
    } else if(o != null) {
      throw new RuntimeException("wrong type " + o.getClass().getName());
    } else {
      return JsonNull.INSTANCE;
    }
  }

}
