package net.qldarch.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.qldarch.guice.Bind;

@Bind
@Singleton
@Slf4j
public class Db {

  private final ConnectionProvider cp;

  private ThreadLocal<Connection> transaction = new ThreadLocal<>();

  @Inject
  public Db(ConnectionProvider cp) {
    this.cp = cp;
  }

  public <T> T executeQuery(String query, ResultSetConsumer<T> consumer) throws Exception {
    return executeQuery(query, null, consumer);
  }

  public <T> T executeQuery(String query, ResultSetConsumer<T> consumer,
      Connection con) throws Exception {
    return executeQuery(query, null, consumer, con);
  }

  public <T> T executeQuery(String query, Map<String, Object> params,
      ResultSetConsumer<T> consumer) throws Exception {
    return execute(con -> executeQuery(query, params, consumer, con));
  }

  public <T> T executeQuery(String query, Map<String, Object> params,
      ResultSetConsumer<T> consumer, Connection con) throws Exception {
    try (NamedParameterStatement stmt = 
        new NamedParameterStatement(con, query, toSqlValues(params))) {
      return consumer.accept(stmt.executeQuery());
    }
  }

  public boolean execute(String statement, Map<String, Object> params) throws Exception {
    return execute(con -> execute(statement, params, con));
  }

  public boolean execute(String statement) throws Exception {
    return execute(con -> execute(statement, null, con));
  }

  public boolean execute(String statement, Map<String, Object> params,
      Connection con) throws SQLException {
    try (NamedParameterStatement stmt = 
        new NamedParameterStatement(con, statement, toSqlValues(params))) {
      return stmt.execute();
    }
  }

  Map<String, SqlValue> toSqlValues(Map<String, Object> map) {
    return map!=null?map.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey, me -> SVal.ofUnknownType(me.getValue()))):null;
  }

  public <T> T execute(Work<T> w) throws Exception {
    final Connection con = transaction.get();
    if(con != null) {
      log.debug("joining transaction");
      return w.run(con);
    } else {
      return executeNew(w);
    }
  }

  public void executeVoid(final VoidWork w) throws Exception {
    execute(new Work<Object>() {
      @Override
      public Object run(Connection con) throws Exception {
        w.run(con);
        return null;
      }});
  }

  private <T> T executeNew(Work<T> w) throws Exception {
    long startms = System.currentTimeMillis();
    Connection con = null;
    try {
      con = cp.con();
      log.debug("start transaction on connection from provider");
      transaction.set(con);
      con.setAutoCommit(false);
      return w.run(con);
    } catch(Exception e) {
      if(con != null) {
        try {con.rollback();} catch(Exception e2) {e2.printStackTrace();}
      }
      throw e;
    } finally {
      transaction.remove();
      if(con != null) {
        try {con.commit();} catch(Exception e) {e.printStackTrace();}
        try {con.close();} catch(Exception e) {e.printStackTrace();}
      }
      long duration = System.currentTimeMillis() - startms;
      log.debug("transaction took {} seconds to complete", duration/1000.0);
    }
  }

  public void join(Connection con, Runnable r) {
    if(transaction.get() != null) {
      log.debug("joining transaction on connection passed in (connection already registered)");
      r.run();
    } else {
      long startms = System.currentTimeMillis();
      try {
        log.debug("joining transaction on connection passed in");
        transaction.set(con);
        r.run();
      } catch(Exception e) {
        if(con != null) {
          try {con.rollback();} catch(Exception e2) {e2.printStackTrace();}
        }
        throw new RuntimeException("caught exception in transaction", e);
      } finally {
        transaction.remove();
      }
      long duration = System.currentTimeMillis() - startms;
      log.debug("leaving transaction after {} seconds", duration/1000.0);
    }
  }

}
