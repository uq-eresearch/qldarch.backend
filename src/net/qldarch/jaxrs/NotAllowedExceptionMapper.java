package net.qldarch.jaxrs;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {
  @Override
  public Response toResponse(NotAllowedException e) {
    return Response.status(405).build();
  }
}
