package net.qldarch.gson.serialize;

public class CustomTypeRegistration extends StdSerializerRegistration {

  public CustomTypeRegistration(ClassSerializable serializable, Serializer serializer) {
    super(serializable, null, serializer);
  }

}
