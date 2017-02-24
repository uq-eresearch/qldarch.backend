package net.qldarch.security;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Guice;
import net.qldarch.hibernate.HS;

@UpdateEntity
@Provider
@Slf4j
public class UpdateEntityFilter implements ContainerRequestFilter {

  @Context
  private ResourceInfo resourceInfo;

  @Inject
  private HS hs;

  private void deny(ContainerRequestContext crc) {
    crc.abortWith(Response.status(403).build());
  }

  private void abort(ContainerRequestContext crc) {
    crc.abortWith(Response.status(500).build());
  }

  private Long getIdFromRequest(ContainerRequestContext crc) {
    try {
      return new Long(crc.getUriInfo().getPathParameters().getFirst("id"));
    } catch(Exception e) {
      return null;
    }
  }

  @Override
  public void filter(ContainerRequestContext crc) throws IOException {
    User user = Guice.injector().getInstance(User.class);
    if(user == null) {
      log.debug("no user logged in , deny update...");
      deny(crc);
    } else if(user.isAdmin()) {
      log.debug("admin user allowed to edit");
    } else if(user.isEditor()) {
      final Long id = getIdFromRequest(crc);
      if(id != null) {
        final Method method = resourceInfo.getResourceMethod();
        if(method != null) {
          UpdateEntity udateEntity = method.getAnnotation(UpdateEntity.class);
          if(udateEntity != null) {
            final Class<? extends Updatable> cls = udateEntity.entityClass();
            Updatable o = hs.get(cls, id);
            if(o != null) {
              if(o.canUpdate(user)) {
                // editor can update,go ahead...
              } else {
                log.info("editor with id '{}' failed can update check for entity '{}/{}', deny",
                    user.getId(), cls.getName(), id, o);
                deny(crc);
              }
            } else {
              log.debug("entity is null for type '{}', id '{}', deny",
                  cls.getName(), id);
              deny(crc);
            }
          } else {
            log.warn("expected an udateEntity on method '{}', abort...", method.getName());
            abort(crc);
          }
        } else {
          log.warn("requested method null, abort update...");
          abort(crc);
        }
      } else {
        log.debug("could not get id from request, deny");
        deny(crc);
      }
    } else if(user.isReader()) {
      log.debug("user with role reader not allowed to update, deny update...");
      deny(crc);
    } else {
      log.warn("unexpected user role, abort");
      abort(crc);
    }
  }

}
