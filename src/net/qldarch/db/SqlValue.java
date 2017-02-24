package net.qldarch.db;

public interface SqlValue {

  public Object value();

  /**
   * The java.sql.Types type of this value
   */
  public int type();
}
