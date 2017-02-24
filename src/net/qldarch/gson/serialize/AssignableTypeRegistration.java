package net.qldarch.gson.serialize;

public class AssignableTypeRegistration extends StdSerializerRegistration {

  public AssignableTypeRegistration(Class<?> type, Serializer serializer) {
    super(cls -> type.isAssignableFrom(cls), null, serializer);
  }

}
