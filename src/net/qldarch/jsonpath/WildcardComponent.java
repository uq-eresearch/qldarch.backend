package net.qldarch.jsonpath;

import lombok.ToString;

@ToString
public class WildcardComponent implements Component {

  @Override
  public boolean matches(Component other) {
    return true;
  }

}
