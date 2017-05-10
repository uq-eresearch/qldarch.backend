package net.qldarch.transcript;

import lombok.Getter;
import lombok.Setter;

public class ParseResult {

  @Getter
  @Setter
  private boolean ok;

  @Getter
  @Setter
  private String msg;

  @Getter
  @Setter
  private String json;

  @Getter
  @Setter
  private Exception cause;

  @Getter
  @Setter
  private Transcript transcript;

  public ParseResult(String msg, Exception cause) {
    setOk(false);
    setMsg(msg);
    setCause(cause);
  }

  public ParseResult(Transcript transcript, String json) {
    setOk(true);
    setJson(json);
    setTranscript(transcript);
  }
}
