package net.qldarch.message;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Email {

  public static void send(String to, String subject, String content) throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.smtp.host", "smtp.uq.edu.au");
    Session session = Session.getDefaultInstance(properties);
    MimeMessage message = new MimeMessage(session);
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
    message.setFrom(new InternetAddress("no-reply@qldarch.net"));
    message.setSubject(subject);
    message.setContent(content, "text/html; charset=utf-8");
    Transport.send(message);
  }

  public static void send(List<String> contacts, String subject, String content) throws Exception {
    final Properties properties = new Properties();
    properties.setProperty("mail.smtp.host", "smtp.uq.edu.au");
    Session session = Session.getDefaultInstance(properties);
    MimeMessage message = new MimeMessage(session);
    contacts.forEach(to -> {
      try {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      } catch(Exception e) {
        log.warn("message add recipient failed", e);
      }
    });
    message.setFrom(new InternetAddress("no-reply@qldarch.net"));
    message.setSubject(subject);
    message.setContent(content, "text/html; charset=utf-8");
    Transport.send(message);
  }

}
