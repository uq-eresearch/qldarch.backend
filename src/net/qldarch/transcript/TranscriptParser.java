package net.qldarch.transcript;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class TranscriptParser {

  private static final Pattern TIMESTAMP_TRANSCRIPT_PATTERN = Pattern
      .compile("^\\[?\\s*(\\d\\d?)\\s*\\:\\s*(\\d\\d?)\\s*\\:\\s*(\\d\\d?)\\s*\\]?\\s+(.*)$");

  private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("((?:\\d\\d)(:)(?:[0-5]\\d)(:)(?:[0-5]\\d))");

  private final LineNumberReader reader;

  private boolean steadyTimestamps = true;

  public TranscriptParser(Reader reader) {
    this.reader = new LineNumberReader(reader);
  }

  private static ObjectMapper mapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
    return mapper;
  }

  private String cleanTimestamp(String s) {
    return Exchange.sec2str(Exchange.str2sec(s));
  }

  private String extractTimestamp(String s) {
    String stripped = StringUtils.strip(s);
    Matcher m = TIMESTAMP_TRANSCRIPT_PATTERN.matcher(stripped);
    if(m.matches()) {
      return cleanTimestamp(String.format("%s:%s:%s", m.group(1), m.group(2), m.group(3)));
    } else if(stripped.length() > 8 && TIMESTAMP_PATTERN.matcher(stripped.substring(0, 8)).matches()) {
      return cleanTimestamp(stripped.substring(0, 8));
    } else if(TIMESTAMP_PATTERN.matcher(stripped).matches()) {
      return cleanTimestamp(stripped);
    } else {
      return null;
    }
  }

  private String extractTranscript(String s) {
    String stripped = StringUtils.strip(s);
    Matcher m = TIMESTAMP_TRANSCRIPT_PATTERN.matcher(stripped);
    if(m.matches()) {
      return StringUtils.strip(m.group(4));
    } else if(stripped.length() > 8 && TIMESTAMP_PATTERN.matcher(stripped.substring(0, 8)).matches()) {
      return StringUtils.strip(stripped.substring(8));
    } else if(!TIMESTAMP_PATTERN.matcher(stripped).matches()) {
      return stripped;
    } else {
      return StringUtils.EMPTY;
    }
  }

  private boolean startsWithTimestamp(String s) {
    return extractTimestamp(s) != null;
  }

  public ParseResult parse() {
    try {
      Transcript transcript = parseTranscript();
      return new ParseResult(transcript, mapper().writeValueAsString(transcript));
    } catch(Exception e) {
      String msg = String.format("transcript parse faild on line %s: %s", reader.getLineNumber(), e.getMessage());
      return new ParseResult(msg, e);
    }
  }

  private String cleanSpeaker(String s) {
    return StringUtils.removeEnd(StringUtils.strip(s), ":");
  }

  private void addExchange(Transcript t, Exchange u, String line) {
    u.setTime(extractTimestamp(line));
    u.setTranscript(extractTranscript(line));
    t.addExchange(u, steadyTimestamps);
  }

  private void extendTranscript(Transcript t, String line) {
    Exchange u = t.last();
    u.appendTranscript(extractTranscript(line));
  }

  private Exchange continueLastSpeaker(Transcript transcript) {
    Exchange u = transcript.last();
    return (u != null) ? new Exchange(u.getSpeaker()) : null;
  }

  private boolean command(String line) {
    String[] tmp = StringUtils.split(line);
    if((tmp.length) >= 1 && "set".equalsIgnoreCase(tmp[0])) {
      if(tmp.length >= 2 && "steady_timestamps".equalsIgnoreCase(tmp[1])) {
        if(tmp.length >= 3 && "off".equalsIgnoreCase(tmp[2])) {
          steadyTimestamps = false;
        } else {
          steadyTimestamps = true;
        }
        return true;
      }
    }
    return false;
  }

  private Transcript parseTranscript() throws IOException {
    Transcript transcript = new Transcript();
    String line;
    Exchange exchange = null;
    boolean repeatSpeaker = false;
    while((line = reader.readLine()) != null) {
      if(StringUtils.isBlank(line) || StringUtils.startsWith(line, "#")) {
        continue;
      }
      line = StringUtils.strip(line);
      if(command(line)) {
        continue;
      }
      // ignore the rest if end of transcript marker is found
      if("END OF TRANSCRIPT".equalsIgnoreCase(StringUtils.strip(line))) {
        extendTranscript(transcript, line);
        exchange = null;
        break;
      }
      // continue current utterance
      if(exchange != null) {
        if(line.length() < 4 && StringUtils.isAllUpperCase(line)) {
          exchange = new Exchange(cleanSpeaker(line));
          repeatSpeaker = false;
        } else {
          if(startsWithTimestamp(line)) {
            if(repeatSpeaker) {
              exchange = continueLastSpeaker(transcript);
            } else {
              repeatSpeaker = true;
            }
            addExchange(transcript, exchange, line);
          } else {
            if(!(line.length() < 4 && StringUtils.isAllUpperCase(line))) {
              extendTranscript(transcript, line);
              repeatSpeaker = true;
            }
          }
        }
      } else if(!transcript.hasTitle()) {
        transcript.setTitle(line);
      } else if(!transcript.hasDate()) {
        transcript.setDate(line);
      } else {
        // start new utterance
        if(startsWithTimestamp(line)) {
          exchange = continueLastSpeaker(transcript);
          if(exchange == null) {
            exchange = new Exchange("no speaker info");
          }
          addExchange(transcript, exchange, line);
        } else {
          if(line.length() < 4 && StringUtils.isAllUpperCase(line)) {
            // speaker line
            exchange = new Exchange(cleanSpeaker(line));
          }
        }
      }
    }
    return transcript;
  }
}
