package net.qldarch.search;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import com.google.gson.JsonElement;

import net.qldarch.gson.serialize.Context;
import net.qldarch.gson.serialize.Serializer;

public class DocumentTypeAdapter implements Serializer {

  private Map<String, Object> fields(Document d) {
    return d.getFields().stream().collect(Collectors.<IndexableField, String, Object>toMap(
        field -> field.name(), field -> {
          final String s = field.stringValue();
          // TODO looking at the string value to determine if it is a boolean is not really great!
          // can you somehow store metadata to a field?
          // otherwise maybe add a meta field to the document (that describes the types)?
          // this field would only be stored but not indexed
          if(StringUtils.equals(s, "true") || StringUtils.equals(s, "false")) {
            return Boolean.valueOf(s);
          } else {
            return ((field.numericValue()!=null)?field.numericValue():field.stringValue());
          }
        }));
  }

  @Override
  public JsonElement serialize(Object o, Context ctx) {
    return ctx.serialize(fields((Document)o));
  }
}
