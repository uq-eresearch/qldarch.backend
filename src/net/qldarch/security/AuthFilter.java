package net.qldarch.security;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Guice;

@Slf4j
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {

  @Context
  private ResourceInfo resourceInfo;

  private String path(ContainerRequestContext crc) {
    return ((crc != null) && (crc.getUriInfo() != null) &&
        (crc.getUriInfo().getAbsolutePath() != null))?
            crc.getUriInfo().getAbsolutePath().toString():"";
  }

  private <T extends Annotation> T annotation(Method m, Class<T> cls) {
    T annotation = m.getAnnotation(cls);
    if(annotation == null) {
      annotation = m.getDeclaringClass().getAnnotation(cls);
    }
    return annotation;
  }

  private AuthResult check(User user, Method theMethod, String path) {
    if(theMethod == null) {
      log.warn("couldn't determine webservice method for request {}", path);
    } else {
      Admin admin = annotation(theMethod, Admin.class);
      if(admin != null) {
        if((user == null) || !"admin".equals(user.getRole())) {
          return AuthResult.deny("admin user required");
        }
      }
      SignedIn signedIn = annotation(theMethod, SignedIn.class);
      if((signedIn != null) && (user == null)) {
        return AuthResult.deny("signed-in user required");
      }
    }
    return AuthResult.allow();
  }

  @Override
  public void filter(ContainerRequestContext crc) throws IOException {
    final String path = path(crc);
    User user = Guice.injector().getInstance(User.class);
    Method theMethod = resourceInfo.getResourceMethod();
    AuthResult ar = check(user, theMethod, path);
    if(ar != null) {
      if(ar.isAllow()) {
        if(StringUtils.isNotBlank(ar.getReason())) {
          log.debug("user '{}' allowed access to '{}' because '{}'", user, path, ar.getReason());
        } else {
          log.debug("user '{}' allowed access to '{}'", user, path);
        }
        
      } else {
        if(StringUtils.isNotBlank(ar.getReason())) {
          log.info("user '{}' denied access to '{}' because '{}'", user, path, ar.getReason());
        } else {
          log.info("user '{}' denied access to '{}'", user);
        }
        crc.abortWith(Response.status(403).build());
      }
    } else {
      log.warn("auth check returned with no result for path '{}', allowed", path);
    }
  }
}
