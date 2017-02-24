package net.qldarch.gson.serialize;

public abstract class AbstractSerializerRegistration implements SerializerRegistration {

  private final Serializer serializer;

  public AbstractSerializerRegistration(Serializer serializer) {
    this.serializer = serializer;
  }

  public Serializer getSerializer() {
    return this.serializer;
  }

}
