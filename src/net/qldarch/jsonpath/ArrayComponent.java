package net.qldarch.jsonpath;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ArrayComponent implements Component {

  private final Long index;

  @Override
  public boolean matches(Component other) {
    if(other instanceof WildcardComponent) {
      return true;
    }
    if(other instanceof ArrayComponent) {
      ArrayComponent ac = (ArrayComponent)other;
      return (index == null) || (ac.index == null) || index.equals(ac.index);
    } else {
      return false;
    }
  }

}
