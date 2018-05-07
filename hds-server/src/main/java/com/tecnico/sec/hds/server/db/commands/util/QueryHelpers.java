package com.tecnico.sec.hds.server.db.commands.util;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class QueryHelpers {

  public String getDBUrl() {
    return "jdbc:h2:" + getDBFilePath();
  }

  public String getDBFilePath() {
    String suffix = System.getProperty("server.port", "8080");
    return "./HDSDB" + suffix;
  }

  public <A> A withTransaction(QueryRunner<Connection, A> query) throws DBException {
    try (Connection conn = connection()) {
      try {
        conn.setAutoCommit(false);
        A result = query.apply(conn);
        conn.commit();
        return result;
      } catch (SQLException e) {
        conn.rollback();
        throw new DBException(e.getMessage(), e);
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException(e.getMessage(), e);
    }
  }

  public <A> A withConnection(QueryRunner<Connection, A> query) throws DBException {
    try (Connection conn = connection()) {
      return query.apply(conn);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException(e.getMessage(), e);
    }
  }

  private Connection connection() throws SQLException {
    String url = getDBUrl();
    return DriverManager.getConnection(url);
  }

}
