package net.qldarch.jsonpath;

import lombok.ToString;

@ToString
public class RootComponent implements Component {

  @Override
  public boolean matches(Component other) {
    return other instanceof RootComponent;
  }

}
