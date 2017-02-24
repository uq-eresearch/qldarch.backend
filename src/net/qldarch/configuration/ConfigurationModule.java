package net.qldarch.configuration;

import java.util.Collections;

import javax.servlet.ServletContext;

import com.google.inject.AbstractModule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class ConfigurationModule extends AbstractModule {

  private ServletContext ctx;

  @Override
  protected void configure() {
    Collections.list(ctx.getInitParameterNames()).forEach(name -> {
      bindConstant().annotatedWith(cfg(name)).to(ctx.getInitParameter(name));
      log.info("binding configuration key {}", name);
    });
  }

  private Cfg cfg(final String name) {
    return new CfgImpl(name);
  }

}
