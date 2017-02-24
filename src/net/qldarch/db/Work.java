package net.qldarch.db;

import java.sql.Connection;

@FunctionalInterface
public interface Work<T> {

  public T run(Connection con) throws Exception;

}
