package net.qldarch.db;

import java.sql.Connection;

@FunctionalInterface
public interface VoidWork {
  void run(Connection con) throws Exception; 
}
