package net.qldarch.security;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.shiro.authc.credential.DefaultPasswordService;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.configuration.Cfg;
import net.qldarch.db.Db;
import net.qldarch.db.Rsc;
import net.qldarch.guice.Bind;
import net.qldarch.util.M;
import net.qldarch.util.RandomString;

@Bind
@Slf4j
public class SignUp {

  @Inject
  private Db db;

  @Inject
  private RandomString rstr;

  @Inject @Cfg("baseUrl")
  private String baseUrl;

  @Inject
  private UserStore users;

  @Inject @Cfg("smtp.host")
  private String smtpHost;

  private String activationCode() {
    return rstr.next();
  }

  public SignUpResponse signup(String email, String displayname, String password) {
    return signup(email, email, displayname, password);
  }

  private void sendActivationEmail(final String email, final long id, final String activationCode) {
    try {
      final Properties properties = new Properties();
      properties.setProperty("mail.smtp.host", smtpHost);
      Session session = Session.getDefaultInstance(properties);  
      MimeMessage message = new MimeMessage(session);  
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
      message.setFrom(new InternetAddress("no-reply@qldarch.net"));
      message.setSubject("Qldarch Account Activation");
      final String content = "Click <a href=\"%s/ws/rest/user/activation?id=%s&code=%s\">here"
          + "</a> to activate your account.";
      message.setContent(String.format(content, baseUrl, id, activationCode), "text/html; charset=utf-8");
      Transport.send(message);
    } catch (Exception e) {
      log.warn("account activation email failed", e);
    }
  }

  private SignUpResponse signup(String name, String email, String displayname, String password) {
    if(isBlank(email) || isBlank(displayname) || isBlank(password)) {
      return SignUpResponse.failed("Mandatory field missing");
    }
    final User u = users.get(name);
    if(u != null) {
      return SignUpResponse.failed(String.format("user name '%s' already assigned", name));
    }
    final String stmt = "insert into appuser(name, email, displayname, password, activation)"
        + " values(:name, :email, :displayname, :password, :activation) returning id";
    try {
      final DefaultPasswordService passwordService = new DefaultPasswordService();
      final String hashedPassword = passwordService.encryptPassword(password);
      final String activationCode = activationCode();
      // TODO check if the username is available and return a better error message if it is not!
      long id = db.executeQuery(stmt, M.of("name", name, "email", email, "displayname",
          displayname, "password", hashedPassword, "activation", activationCode), Rsc::singleIntegral);
      sendActivationEmail(email, id, activationCode);
      return new SignUpResponse(true, "ok");
    } catch(Exception e) {
      log.warn("signup failed", e);
      return SignUpResponse.failed("Sign-up failed, unknown reason");
    }
  }

}
