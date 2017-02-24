package net.qldarch.db;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetConsumer<T> {

  T accept(ResultSet resultset) throws Exception;

}
