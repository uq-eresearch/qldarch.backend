package net.qldarch.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.qldarch.guice.Bind;


@Bind(to={DbDataSource.class, ConnectionProvider.class})
class JndiDbDataSource implements DbDataSource {

  public DataSource datasource() throws NamingException {
    InitialContext cxt = new InitialContext();
    DataSource ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/qldarch");
    if ( ds == null ) {
       throw new RuntimeException("data source not found!");
    }
    return ds;
  }

  public Connection con() throws NamingException, SQLException {
    Connection con = datasource().getConnection();
    if(con != null) {
      return con;
    } else {
      throw new RuntimeException("failed to get connection from datasource (null)");
    }
  }

}
