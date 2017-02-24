package net.qldarch.jsonpath;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class JsonPathParser {

  public JsonPath parse(String path) {
    try {
      return new JsonPath(components(scan(path)));
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to parse string into json path '%s'", path));
    }
  }

  private boolean endOfStream(int i) {
    return (i == 65535) || (i == -1);
  }

  private String readUntil(PushbackReader stream, char[] until) throws Exception {
    StringBuilder builder = new StringBuilder();
    for(int i = stream.read();!endOfStream(i);i = stream.read()) {
      char c = (char)i;
      if(ArrayUtils.contains(until, c)) {
        stream.unread(i);
        break;
      }
      builder.append(c);
    }
    return builder.toString();
  }

  private List<Token> scan(String path) {
    try {
      PushbackReader stream = new PushbackReader(new StringReader(path), 2); 
      List<Token> tokens = new ArrayList<>();
      for(int i = stream.read();!endOfStream(i);i = stream.read()) {
        int next = stream.read();
        stream.unread(next);
        if(i == '$') {
          tokens.add(new Token(TokenType.root));
        } else if((i == '.') && (next == '.')) {
          tokens.add(new Token(TokenType.descent));
        } else if(i == '.') {
          tokens.add(new Token(TokenType.dot));
        } else if(i == '[') {
          tokens.add(new Token(TokenType.bracketstart));
        } else if(i == ']') {
          tokens.add(new Token(TokenType.bracketend));
        } else if(i == '*') {
          tokens.add(new Token(TokenType.wildcard));
        } else if(i == '@') {
          tokens.add(new Token(TokenType.current));
        } else if(i == '\'') {
          tokens.add(new Token(TokenType.stringliteral, readUntil(stream, new char[] {'\''})));
          // remove the ' character from stream
          stream.read();
        } else {
          stream.unread(i);
          tokens.add(new Token(TokenType.literal, readUntil(stream, new char[] {'.', '[', ']'})));
        }
      }
      return tokens;
    } catch(Exception e) {
      throw new RuntimeException(String.format("failed to parse json path '%s'", path), e);
    }
  }

  private static Long tryLong(String s) {
    try {
      return new Long(s);
    } catch(Exception e) {
      return null;
    }
  }

  private List<Component> components(List<Token> tokens) {
    List<Component> components = new ArrayList<>();
    for(Token t : tokens) {
      if(t.type.equals(TokenType.root)) {
        components.add(new RootComponent());
      } else if(t.type.equals(TokenType.wildcard)) {
        components.add(new WildcardComponent());
      } else if(t.type.equals(TokenType.stringliteral)) {
        components.add(new ChildComponent(t.attribute));
      } else if(t.type.equals(TokenType.literal)) {
        Long l = tryLong(t.attribute);
        if(l != null) {
          components.add(new ArrayComponent(l));
        } else {
          components.add(new ChildComponent(t.attribute));
        }
      }
    }
    return components;
  }
}
