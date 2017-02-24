package net.qldarch.gson;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.gson.GsonBuilder;

import net.qldarch.gson.serialize.JsonSetup;

@Provider
@Produces({MediaType.APPLICATION_JSON, "text/json"})
public class GsonMessageBodyWriter implements MessageBodyWriter<Object> {

  @Context
  private HttpServletRequest request;

  @Inject
  private GsonSetup gson;

  @Override
  public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3,
      MediaType arg4) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return true;
  }

  private GsonBuilder pretty(GsonBuilder builder) {
    if((request!=null) && (request.getParameter("pretty")!=null)) {
      builder.setPrettyPrinting();
    }
    return builder;
  }

  @Override
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    OutputStreamWriter writer = new OutputStreamWriter(entityStream);
//    log.debug("gson.toJson type {}, genericType {}", type, genericType);
//     using the generic type does not work with the compound object call
//     gson.toJson type class net.qldarch.compound.Timeline, genericType class net.qldarch.compound.CompoundObject
//    gson(annotations).toJson(t, genericType, writer);
    pretty(gson.setup(annotations)).create().toJson(
        new JsonSetup().setup(annotations).toJsonTree(t), writer);
    writer.flush();
  }
}
