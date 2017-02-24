package net.qldarch.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.persistence.Entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.reflections.Reflections;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.configuration.Cfg;
import net.qldarch.guice.Bind;

@Bind(eagerSingleton=true)
@Slf4j
public class HS {

  private SessionFactory sessionFactory;

  private ThreadLocal<Session> sessions = new ThreadLocal<>();

  @Inject
  public HS(final net.qldarch.db.ConnectionProvider cp, @Cfg("hibernate.show.sql") boolean showSql) {
    final StandardServiceRegistryBuilder registry = new StandardServiceRegistryBuilder();
    registry.applySetting(AvailableSettings.CONNECTION_PROVIDER, cp(cp));
    registry.applySetting(AvailableSettings.SHOW_SQL, showSql);
    sessionFactory = configure().buildSessionFactory(registry.build());
  }

  private ConnectionProvider cp(final net.qldarch.db.ConnectionProvider cp) {
    return new ConnectionProvider() {

      @SuppressWarnings("rawtypes")
      @Override
      public boolean isUnwrappableAs(Class cls) {
        log.debug("isUnwrappableAs {}", cls.getName());
        return false;
      }

      @Override
      public <T> T unwrap(Class<T> cls) {
        log.debug("unwrap {}", cls.getName());
        return null;
      }

      @Override
      public void closeConnection(Connection con) throws SQLException {
        log.debug("close");
        con.close();
      }

      @Override
      public Connection getConnection() throws SQLException {
        log.debug("getConnection");
        try {
          return cp.con();
        } catch(Exception e) {
          throw new SQLException("failed to get connection", e);
        }
      }

      @Override
      public boolean supportsAggressiveRelease() {
        log.debug("supportsAggressiveRelease");
        return false;
      }};
  }

  private Configuration configure() {
    final Configuration cfg = new Configuration();
    new Reflections("net.qldarch").getTypesAnnotatedWith(Entity.class).forEach(cls -> {
      log.info("register annotated jpa entity {}", cls.getName());
      cfg.addAnnotatedClass(cls);
    }); 
    return cfg;
  }

  private Session openSession() {
    return sessionFactory.openSession();
  }

  public <T> T execute(HWork<T> work) {
    Session session = sessions.get();
    if(session != null) {
      log.debug("HS, attach work to current session");
      return work.run(session);
    } else {
      log.debug("HS, start new session");
      return executeNew(work);
    }
  }

  public void executeVoid(HVoidWork work) {
    execute(session -> {
      work.run(session);
      return null;
    });
  }

  public <T> T executeNew(HWork<T> work) {
    long startms = System.currentTimeMillis();
    try(Session session = openSession()) {
      sessions.set(session);
      Transaction tx = null;
      try {
        tx = session.beginTransaction();
        T result = work.run(session);
        tx.commit();
        return result;
      } catch(Exception e) {
       if(tx != null) {
         tx.rollback();
       }
       throw new RuntimeException("failed to execute hibernate work", e);
      }
    } finally {
      sessions.remove();
      log.debug("session active for  {} seconds", ((System.currentTimeMillis() - startms)/1000.0));
    }
  }

  public <T> T get(Class<T> entityType, Serializable id) {
    return execute(session -> session.get(entityType, id));
  }

  public void update(Object o) {
    executeVoid(session -> session.update(o));
  }

  public void save(Object o) {
    executeVoid(session -> session.save(o));
  }

}
