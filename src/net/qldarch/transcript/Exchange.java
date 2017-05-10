package net.qldarch.transcript;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Exchange {

  @Getter
  @Setter
  private String speaker;

  @Getter
  @Setter
  private String time;

  @Getter
  @Setter
  private String transcript;

  public Exchange(String speaker) {
    setSpeaker(speaker);
  }

  void appendTranscript(String transcript) {
    String whiteSpace = "\\s";
    String prevTscp = getTranscript();
    String addedTscp;
    if(prevTscp.length() > 0 && !prevTscp.substring(prevTscp.length() - 1).matches(whiteSpace)
        && transcript.length() > 0 && !transcript.substring(0, 1).matches(whiteSpace)) {
      addedTscp = StringUtils.leftPad(transcript, transcript.length() + 1);
    } else {
      addedTscp = transcript;
    }
    setTranscript(prevTscp + addedTscp);
  }

  public long getSeconds() {
    return str2sec(getTime());
  }

  String getTranscriptShort() {
    return shorten(getTranscript(), 20);
  }

  static long str2sec(String s) {
    String[] tmp = StringUtils.split(s, ':');
    ArrayUtils.reverse(tmp);
    long[] mul = { 1, 60, 3600 };
    if(tmp.length > mul.length) {
      throw new RuntimeException("can't convert timestamp " + s);
    }
    long result = 0;
    for(int i = 0; i < tmp.length; i++) {
      long l = Long.parseLong(tmp[i]);
      result += l * mul[i];
    }
    return result;
  }

  private static String pad0(long l) {
    String s = Long.toString(l);
    if(s.length() == 1) {
      return "0" + s;
    } else {
      return s;
    }
  }

  static String sec2str(long sec) {
    long hour = sec / 3600;
    long minute = (sec - hour * 3600) / 60;
    long second = sec - hour * 3600 - minute * 60;
    return String.format("%s:%s:%s", pad0(hour), pad0(minute), pad0(second));
  }

  static String shorten(String s, int max) {
    return (StringUtils.isBlank(s) || (s.length() <= max)) ? s : StringUtils.substring(s, 0, max) + "...";
  }
}
