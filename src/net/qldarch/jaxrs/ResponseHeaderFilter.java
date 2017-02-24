package net.qldarch.jaxrs;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

@Provider
public class ResponseHeaderFilter implements ContainerResponseFilter {

  @Context
  private ResourceInfo resourceInfo;

  private void addHeader(ResponseHeader header, ContainerResponseContext responseCtx) {
    String s = header.value();
    if(s.indexOf(':') > 0) {
      String name = s.substring(0, s.indexOf(':'));
      String value = s.substring(s.indexOf(':')+1);
      responseCtx.getHeaders().add(StringUtils.strip(name), StringUtils.strip(value));
    }
  }

  private Method method() {
    try {
      return resourceInfo.getResourceMethod();
    } catch(RuntimeException e) {
      // this happens e.g.when the responseCtx has a status of 404
      return null;
    }
  }

  @Override
  public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx)
      throws IOException {
    Method theMethod = method();
    if(theMethod != null) {
      if(theMethod.getAnnotation(ResponseHeaders.class)!= null) {
        for(ResponseHeader header : theMethod.getAnnotation(ResponseHeaders.class).value()) {
          addHeader(header, responseCtx);
        }
      } else if(theMethod.getAnnotation(ResponseHeader.class) != null) {
        addHeader(theMethod.getAnnotation(ResponseHeader.class), responseCtx);
      } else {
      }
    }
  }

}
