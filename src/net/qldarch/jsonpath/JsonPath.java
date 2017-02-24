package net.qldarch.jsonpath;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * very incomplete implementation of json path only for the 
 * purpose of checking if 2 json paths match. this is used for the custom net.qldarch json
 * serializer as it allows to attach serializers to a type or a path.
 **/
@AllArgsConstructor
@ToString
public class JsonPath {

  private List<Component> components;

  public boolean matches(JsonPath other) {
    if(this.components.size() == other.components.size()) {
      for(int i=0;i<components.size();i++) {
        if(!components.get(i).matches(other.components.get(i))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public static JsonPath parse(String path) {
    return new JsonPathParser().parse(path);
  }

}
