package net.qldarch.message;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;
import net.qldarch.security.UserStore;

import org.apache.commons.lang3.StringUtils;

@Bind
@Slf4j
public class MessageContact {

  @Inject
  private UserStore users;

  public MessageContactResponse send(String content, String senderName, String from, boolean newsletter) {
    try {
      String msgsubject = "[QLDArch.net] Message from " + senderName + " <" + from + ">";
      log.info("message subject: {}", msgsubject);
      String msgcontent = (content + (newsletter ? "<br/>(Please send me the latest news via email)"
          : StringUtils.EMPTY)).replace("\n", "<br/>");
      log.info("message content: {}", msgcontent);
      List<String> contacts = new ArrayList<>();
      users.all().forEach(user -> {
        if(user.isContact()) {
          try {
            contacts.add(user.getEmail());
            log.info("added user to contacts: {}", user.getEmail());
          } catch(Exception e) {
            log.warn("adding user to contacts failed", e);
          }
        }
      });
      Email.send(contacts, msgsubject, msgcontent);
      return new MessageContactResponse(true, "ok");
    } catch(Exception e) {
      log.warn("message contact failed", e);
      return MessageContactResponse.failed("message contact failed, unknown reason");
    }
  }
}
