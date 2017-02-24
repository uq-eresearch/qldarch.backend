package net.qldarch.db;

import javax.sql.DataSource;

interface DbDataSource extends ConnectionProvider {

  DataSource datasource() throws Exception;

}
