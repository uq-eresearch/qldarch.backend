package net.qldarch.guice;

import com.google.inject.Injector;

public class Guice {

  private static Injector injector;

  public static void setInjector(Injector injector) {
    Guice.injector = injector;
  }

  public static Injector injector() {
    return injector;
  }

}
