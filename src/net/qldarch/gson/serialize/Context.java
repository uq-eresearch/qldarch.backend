package net.qldarch.gson.serialize;

import java.util.IdentityHashMap;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public class Context {

  private String path;

  private List<Serializer> delegates;

  private Json json;

  private IdentityHashMap<Object, Object> visited;

  public Context(String path, List<Serializer> delegates, Json json, IdentityHashMap<Object, Object> visited) {
    this.path = path;
    this.delegates = delegates;
    this.json = json;
    this.visited = visited;
  }

  public String getPath() {
    return path;
  }

  public JsonElement delegate(Object o) {
    if(delegates.isEmpty()) {
      return JsonNull.INSTANCE;
    } else {
      return delegates.get(0).serialize(o, new Context(path, Utils.tail(delegates), json, visited));
    }
  }

  public JsonElement serialize(Object o, String path) {
    return json.toJsonTree(o, this.path+path, this);
  }

  public JsonElement serialize(Object o) {
    return json.toJsonTree(o, this.path, this);
  }

  public IdentityHashMap<Object, Object> visited() {
    return visited;
  }

}
