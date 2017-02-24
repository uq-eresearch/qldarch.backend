package net.qldarch.jsonpath;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class ChildComponent implements Component {

  private final String name;

  @Override
  public boolean matches(Component other) {
    if(other instanceof WildcardComponent) {
      return true;
    }
    if(other instanceof ChildComponent) {
      ChildComponent cc = (ChildComponent)other;
      return StringUtils.equals(name, cc.name);
    } else {
      return false;
    }
  }

}
