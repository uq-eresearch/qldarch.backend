package net.qldarch.db;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionProvider {

  public Connection con() throws Exception;

}
