package net.qldarch.jsonpath;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Token {

  public final TokenType type;

  public final String attribute;

  public Token(TokenType type) {
    this(type, null);
  }

}
