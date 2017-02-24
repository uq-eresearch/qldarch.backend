package net.qldarch.resteasy;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.Version;
import net.qldarch.configuration.ConfigurationModule;
import net.qldarch.guice.AutoBindModule;
import net.qldarch.guice.Guice;
import net.qldarch.hibernate.HsFilter;

@Slf4j
@WebListener
public class ContextListener extends GuiceResteasyBootstrapServletContextListener {

  // I'm afraid GuiceServletContextListener is more then a convenience utility
  // (https://github.com/google/guice/wiki/ServletModule)
  // as it does setup the InternalServletModule.BackwardsCompatibleServletContextProvider
  // which if not initialized gives a rather bizarre warning. Unfortunately the class is 
  // package private so we call into the GuiceServletContextListener.contextInitialized
  // as soon as the injector is made available by the GuiceResteasyBootstrapServletContextListener
  private static class GuiceSCL extends GuiceServletContextListener {

    private Injector injector;

    public GuiceSCL(Injector injector) {
      this.injector = injector;
    }

    @Override
    protected Injector getInjector() {
      return injector;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
      super.contextInitialized(servletContextEvent);
      new Reflections("net.qldarch").getSubTypesOf(Lifecycle.class).forEach(cls -> {
        injector.getInstance(cls).start();
      });
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
      super.contextDestroyed(servletContextEvent);
      new Reflections("net.qldarch").getSubTypesOf(Lifecycle.class).forEach(cls -> {
        injector.getInstance(cls).stop();
      });
    }

  }

  private GuiceSCL guiceSCL;

  private ServletContextEvent ctxEvent;

  @Override
  public void contextInitialized(ServletContextEvent ctxEvent) {
    this.ctxEvent = ctxEvent;
    log.info("starting qldarch.backend version {}", new Version().get());
    super.contextInitialized(ctxEvent);
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    if(guiceSCL != null) {
      guiceSCL.contextDestroyed(event);
    }
    super.contextDestroyed(event);
  }

  @Override
  protected List<? extends Module> getModules(ServletContext ctx) {
    return ImmutableList.<Module>of(new ConfigurationModule(ctx), new AutoBindModule(),
        binder -> bindAll(binder, Path.class), binder -> bindAll(binder, Provider.class),
        new ServletModule() {
          @Override
          protected void configureServlets() {
            filter("/*").through(ExceptionLogFilter.class);
            filter("/*").through(HsFilter.class);
          }
        });
  }

  private void bindAll(Binder binder, Class<? extends Annotation> annotation) {
    new Reflections("net.qldarch").getTypesAnnotatedWith(annotation).forEach(cls -> binder.bind(cls));
  }

  @Override
  protected void withInjector(Injector injector) {
    Guice.setInjector(injector);
    guiceSCL = new GuiceSCL(injector);
    guiceSCL.contextInitialized(ctxEvent);
  }

}
