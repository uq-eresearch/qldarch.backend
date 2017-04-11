package net.qldarch.security;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.hibernate.HS;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;

@Path("/account/password")
@Slf4j
public class WsChangePassword {

  @Inject
  @Nullable
  private User user;

  @Inject
  private HS hs;

  private String encrypt(String s) {
    final DefaultPasswordService passwordService = new DefaultPasswordService();
    return passwordService.encryptPassword(s);
  }

  public boolean change(String password, String confirmPassword) {
    if((user != null) && StringUtils.equals(password, confirmPassword)) {
      try {
        final String encryptedPassword = encrypt(StringUtils.strip(password));
        user.setPassword(encryptedPassword);
        hs.update(user);
        log.info("change password of appuser {} successful", user.getUsername());
        return true;
      } catch(Exception e) {
        log.debug("failed to change password for appuser id {}", user.getId(), e);
      }
    }
    return false;
  }

  @PUT
  public Response post(@FormParam("password") String password, @FormParam("confirmPassword") String confirmPassword) {
    if(change(password, confirmPassword)) {
      return Response.ok().build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }
}
