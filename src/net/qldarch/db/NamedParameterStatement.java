package net.qldarch.db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

// from http://www.javaworld.com/article/2077706/core-java/named-parameters-for-preparedstatement.html?page=2
// fixed issue with double colon
// refactored
@Slf4j
class NamedParameterStatement implements AutoCloseable {

  @FunctionalInterface
  private static interface Setter {
    public void set(int i) throws SQLException;
  }

  private static class SqlExceptionWrapper extends RuntimeException {
    private SQLException wrapped;
    SqlExceptionWrapper(SQLException e) {
      super(e);
      this.wrapped = e;
    }

    SQLException wrapped() {
      return wrapped;
    }
  }

  /** The statement this object is wrapping. */
  private final PreparedStatement statement;

  /** Maps parameter names to arrays of ints which are the parameter indices. 
   */
  private final Map<String, int[]> indexMap = Maps.newHashMap();

  private final String query;

  /**
   * Creates a NamedParameterStatement.  Wraps a call to
   * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
   * @param connection the database connection
   * @param query      the parameterized query
   * @throws SQLException if the statement could not be created
   */
  public NamedParameterStatement(Connection connection, String query) throws SQLException {
    this(connection, query, null);
  }

  public NamedParameterStatement(Connection connection, String query,
      Map<String, SqlValue> params) throws SQLException {
    log.debug("statement\n{}\nparams {}", query, params);
    this.query = query;
    String parsedQuery=parse(query);
    statement=connection.prepareStatement(parsedQuery);
    setParams(params);
  }

  /**
   * Parses a query with named parameters.  The parameter-index mappings are put into the map, and the
   * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This method is non-private so JUnit code can
   * test it.
   * @param query    query to parse
   * @param paramMap map to hold parameter-index mappings
   * @return the parsed query
   */
  private String parse(String query) {
    int length=query.length();
    StringBuilder parsedQuery=new StringBuilder(length);
    boolean inSingleQuote=false;
    boolean inDoubleQuote=false;
    int index=1;

    for(int i=0;i<length;i++) {
      char c=query.charAt(i);
      if(inSingleQuote) {
        if(c=='\'') {
          inSingleQuote=false;
        }
      } else if(inDoubleQuote) {
        if(c=='"') {
          inDoubleQuote=false;
        }
      } else {
        if(c=='\'') {
          inSingleQuote=true;
        } else if(c=='"') {
          inDoubleQuote=true;
        } else if((c == ':') && (i+1<length) && (query.charAt(i+1) == ':')) {
          // skip double colon
          parsedQuery.append(':');
          parsedQuery.append(':');
          i++;
          continue;
        } else if(c==':' && i+1<length &&
            Character.isJavaIdentifierStart(query.charAt(i+1))) {
          int j=i+2;
          while(j<length && Character.isJavaIdentifierPart(query.charAt(j))) {
            j++;
          }
          String name=query.substring(i+1,j);
          c='?'; // replace the parameter with a question mark
          i+=name.length(); // skip past the end if the parameter
          int[] ilist= indexMap.get(name);
          if(ilist == null) {
            ilist = new int[1];
            ilist[0] = index;
            indexMap.put(name, ilist);
          } else {
            int[] nlist = new int[ilist.length+1];
            System.arraycopy(ilist, 0, nlist, 0, ilist.length);
            nlist[ilist.length] = index;
            indexMap.put(name, nlist);
          }
          index++;
        }
      }
      parsedQuery.append(c);
    }
    return parsedQuery.toString();
  }

  private int[] ilist(String name) {
    int[] ilist = indexMap.get(name);
    if(ilist == null) {
      throw new RuntimeException(String.format("no parameter with name %s in query", name));
    }
    return ilist;
  }

  private void set(String name, Setter setter) throws SQLException {
    try {
      IntStream.of(ilist(name)).forEach(i -> {
        try {
          setter.set(i);
        } catch(SQLException e) {
          throw new SqlExceptionWrapper(e);
        }
      });
    } catch(SqlExceptionWrapper e) {
      throw e.wrapped();
    }
  }

  /**
   * Sets a parameter.
   * @param name  parameter name
   * @param value parameter value
   * @throws SQLException if an error occurred
   * @throws IllegalArgumentException if the parameter does not exist
   * @see PreparedStatement#setObject(int, java.lang.Object)
   */
  public void setObject(String name, Object value) throws SQLException {
    set(name, i -> statement.setObject(i, value));
  }

  public void setArray(String name, Array array) throws SQLException {
    set(name, i -> statement.setArray(i, array));
  }

  public void setNull(String name, int type) throws SQLException {
    set(name, i -> statement.setNull(i, type));
  }

  /**
   * Sets a parameter.
   * @param name  parameter name
   * @param value parameter value
   * @throws SQLException if an error occurred
   * @throws IllegalArgumentException if the parameter does not exist
   * @see PreparedStatement#setString(int, java.lang.String)
   */
  public void setString(String name, String value) throws SQLException {
    set(name, i -> statement.setString(i, value));
  }

  /**
   * Sets a parameter.
   * @param name  parameter name
   * @param value parameter value
   * @throws SQLException if an error occurred
   * @throws IllegalArgumentException if the parameter does not exist
   * @see PreparedStatement#setInt(int, int)
   */
  public void setInt(String name, int value) throws SQLException {
    set(name, i -> statement.setInt(i, value));
  }

  /**
   * Sets a parameter.
   * @param name  parameter name
   * @param value parameter value
   * @throws SQLException if an error occurred
   * @throws IllegalArgumentException if the parameter does not exist
   * @see PreparedStatement#setInt(int, int)
   */
  public void setLong(String name, long value) throws SQLException {
    set(name, i -> statement.setLong(i, value));
  }

  /**
   * Sets a parameter.
   * @param name  parameter name
   * @param value parameter value
   * @throws SQLException if an error occurred
   * @throws IllegalArgumentException if the parameter does not exist
   * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  public void setTimestamp(String name, Timestamp value) throws SQLException {
    set(name, i -> statement.setTimestamp(i, value));
  }

  /**
   * Returns the underlying statement.
   * @return the statement
   */
  public PreparedStatement getStatement() {
    return statement;
  }

  /**
   * Executes the statement.
   * @return true if the first result is a {@link ResultSet}
   * @throws SQLException if an error occurred
   * @see PreparedStatement#execute()
   */
  public boolean execute() throws SQLException {
    return statement.execute();
  }

  /**
   * Executes the statement, which must be a query.
   * @return the query results
   * @throws SQLException if an error occurred
   * @see PreparedStatement#executeQuery()
   */
  public ResultSet executeQuery() throws SQLException {
    return statement.executeQuery();
  }

  /**
   * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE 
statement;
   * or an SQL statement that returns nothing, such as a DDL statement.
   * @return number of rows affected
   * @throws SQLException if an error occurred
   * @see PreparedStatement#executeUpdate()
   */
  public int executeUpdate() throws SQLException {
    return statement.executeUpdate();
  }

  /**
   * Closes the statement.
   * @throws SQLException if an error occurred
   * @see Statement#close()
   */
  @Override
  public void close() throws SQLException {
    statement.close();
  }

  /**
   * Adds the current set of parameters as a batch entry.
   * @throws SQLException if something went wrong
   */
  public void addBatch() throws SQLException {
    statement.addBatch();
  }

  /**
   * Executes all of the batched statements.
   * 
   * See {@link Statement#executeBatch()} for details.
   * @return update counts for each statement
   * @throws SQLException if something went wrong
   */
  public int[] executeBatch() throws SQLException {
    return statement.executeBatch();
  }

  // TODO support more types
  // FIXME this method is a worry
  private String guessType(List<?> list) {
    // TODO try to retrieve the generic type instead of defaulting to 'text'
    if(list.isEmpty()) {
      return "text";
    } else {
      Object o = list.get(0);
      if(o instanceof String) {
        return "text";
      } else if( o instanceof Integer) {
        return "int";
      } else if(o instanceof Long) {
        return "bigint";
      } else if(o instanceof SqlValue) {
        int type = ((SqlValue)o).type();
        if(type == Types.VARCHAR) {
          return "text";
        } else if(type == Types.INTEGER) {
          return "int";
        } else {
          throw new RuntimeException(String.format("array type %s not supported", type));
        }
      } else {
        throw new RuntimeException(String.format("array type %s not supported",
            o.getClass().getName()));
      }
    }
  }

  private Array createArrayOf(SqlValue v) throws SQLException {
    List<?> list = (List<?>)v.value();
//    Object[] oarray = list.toArray(new Object[0]);
//    TODO this works perfectly ok on ubuntu with postgres 9.3.5
//    but not on scientific linux with postgres 9.3.5 (same jdbc driver, WTF?)
//    On SF the jdbc driver returns null :-(
//    NO_LONGER_TODO support other array types (not only text)
//    Array array = con.createArrayOf("text", oarray);
    String type = guessType(list);
    try(PreparedStatement p = statement.getConnection().prepareStatement(
        String.format("select array[%s]::%s[]", StringUtils.repeat("?", ", ", list.size()), type))) {
      int i = 1;
      for(Object o : list) {
        if(o instanceof SqlValue) {
          SqlValue sqlValue = (SqlValue)o;
          p.setObject(i++, sqlValue.value(), sqlValue.type());
        } else {
          p.setObject(i++, o);
        }
      }
      try(ResultSet rs = p.executeQuery()) {
        return rs.next()?rs.getArray(1):null;
      }
    }
  }

  public void setParams(Map<String, SqlValue> params) throws SQLException {
    Set<String> qparams = new HashSet<>(indexMap.keySet());
    if(params != null) {
      params.entrySet().stream().forEach(me -> {
        final String name = me.getKey();
        final SqlValue sval = me.getValue();
        if(sval == null) {
          throw new RuntimeException(String.format("%s has no value", name));
        }
        final Object value = sval.value();
        try {
          if(sval.type() == Types.ARRAY) {
            setArray(name, createArrayOf(sval));
          } else {
            if(value == null) {
              setNull(name, sval.type());
            } else {
              setObject(name, value);
            }
          }
          qparams.remove(name);
        } catch(SQLException e) {
          throw new RuntimeException(String.format(
              "failed to set params on prepared statement %s %s", name, value), e);
        }
      });
    }
    if(!qparams.isEmpty()) {
      throw new RuntimeException(String.format(
          "query parameters missing %s for query:\n%s", qparams, query));
    }
  }
}
