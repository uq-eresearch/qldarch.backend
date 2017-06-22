package net.qldarch.security;

import java.util.Properties;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.configuration.Cfg;
import net.qldarch.hibernate.HS;
import net.qldarch.util.RandomString;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;

@Path("/account/password/reset")
@Slf4j
public class WsResetPassword {

  @Inject
  @Nullable
  private UserStore users;

  @Inject
  private HS hs;

  @Inject
  private RandomString rstr;

  @Inject
  @Cfg("smtp.host")
  private String smtpHost;

  private String encrypt(String s) {
    final DefaultPasswordService passwordService = new DefaultPasswordService();
    return passwordService.encryptPassword(s);
  }

  private boolean reset(String email) {
    User user = users.getByUsernameOrEmail(email);
    try {
      final String password = StringUtils.substring(rstr.next(), 0, 12);
      final String encryptedPassword = encrypt(password);
      user.setPassword(encryptedPassword);
      hs.update(user);
      final String content = String.format("The new password for account '%s' is %s", user.getUsername(), password);
      if(send(content, user)) {
        return true;
      }
    } catch(Exception e) {
      log.debug("failed to reset password for user with id {}, ", user.getId(), e);
    }
    return false;
  }

  @POST
  @Produces("text/html")
  public Response post(@QueryParam("email") String email) {
    if(reset(email)) {
      return Response.status(Response.Status.OK).entity("Your new password has been emailed").build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("An error occured while resetting the password, please try again").build();
    }
  }

  private boolean send(String content, User user) {
    try {
      String msg = (content).replace("\n", "<br/>");
      final Properties properties = new Properties();
      properties.setProperty("mail.smtp.host", smtpHost);
      Session session = Session.getDefaultInstance(properties);
      MimeMessage message = new MimeMessage(session);
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
      message.setFrom(new InternetAddress("no-reply@qldarch.net"));
      message.setSubject("Qldarch Password Reset");
      message.setContent(msg, "text/html; charset=utf-8");
      Transport.send(message);
      return true;
    } catch(Exception e) {
      log.debug("failed to send password for user with id {} and email {}, ", user.getId(), user.getEmail(), e);
      return false;
    }
  }
}
