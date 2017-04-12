package net.qldarch.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MessageContactResponse {

  @Getter
  private final boolean success;

  @Getter
  private final String reason;

  static MessageContactResponse failed(String reason) {
    return new MessageContactResponse(false, reason);
  }

  static MessageContactResponse ok() {
    return new MessageContactResponse(true, null);
  }
}
