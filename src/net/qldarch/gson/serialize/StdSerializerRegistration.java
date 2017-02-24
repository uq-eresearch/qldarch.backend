package net.qldarch.gson.serialize;

import net.qldarch.jsonpath.JsonPath;

public class StdSerializerRegistration extends AbstractSerializerRegistration {

  private final ClassSerializable serializable;

  private final JsonPath path;

  public StdSerializerRegistration(ClassSerializable serializable, JsonPath path, Serializer serializer) {
    super(serializer);
    this.serializable = serializable;
    this.path = path;
  }

  @Override
  public boolean canSerialize(Class<?> cls, JsonPath path) {
    return serializable.canSerialize(cls) && ((this.path == null) || this.path.matches(path));
  }

}
