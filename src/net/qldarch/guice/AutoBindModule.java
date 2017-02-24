package net.qldarch.guice;

import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

@Slf4j
public class AutoBindModule extends AbstractModule {

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void configure() {
    for(Class<?> cls : new Reflections("net.qldarch").getTypesAnnotatedWith(Bind.class)) {
      Bind bind = cls.getAnnotation(Bind.class);
      Class provides = bind.provides();
      if((provides != null) && (provides != None.class)) {
        bind(provides).toProvider(cls);
        log.info(String.format("autobind: binding %s to provider %s",
            provides.getName(), cls.getName()));
      } else {
        for(Class to : bind.to()) {
          StringBuilder msg = new StringBuilder("autobind: binding ");
          ScopedBindingBuilder builder;
          if((to != null) && (to != None.class)) {
            msg.append(String.format("%s to %s", to.getName(), cls.getName()));
            AnnotatedBindingBuilder abuilder = bind(to);
            builder = abuilder.to(cls);
          } else {
            msg.append(cls.getName());
            builder = bind(cls);
          }
          if(bind.eagerSingleton()) {
            msg.append(" as eager singleton");
            builder.asEagerSingleton();
          }
          log.info(msg.toString());
        }
      }
    }
  }

}
