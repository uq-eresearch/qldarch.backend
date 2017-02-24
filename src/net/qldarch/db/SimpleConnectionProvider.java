package net.qldarch.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimpleConnectionProvider implements ConnectionProvider {

  private final String dbname;

  private final String user;

  private final String password;

  @Override
  public Connection con() throws Exception {
    final String url = "jdbc:postgresql://localhost/" + dbname;
    final Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", password);
    return DriverManager.getConnection(url, props);
  }

}
