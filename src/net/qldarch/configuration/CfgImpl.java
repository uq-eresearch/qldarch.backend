package net.qldarch.configuration;

import java.io.Serializable;
import java.lang.annotation.Annotation;


// I could not get an anonymous inner class of the Cfg annotation working with the injector, 
// this is why I've copied from com.google.inject.name.NamedImpl
// The Named Annotation would have worked as well but I wanted
// to create a separate namespace for the configuration
class CfgImpl implements Cfg, Serializable {

  private static final long serialVersionUID = 0;

  private final String value;

  public CfgImpl(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  public int hashCode() {
    return (127 * "value".hashCode()) ^ value.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Cfg)) {
      return false;
    }
    Cfg other = (Cfg) o;
    return value.equals(other.value());
  }

  @Override
  public String toString() {
    return "@" + Cfg.class.getName() + "(value=" + value + ")";
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return Cfg.class;
  }

}

