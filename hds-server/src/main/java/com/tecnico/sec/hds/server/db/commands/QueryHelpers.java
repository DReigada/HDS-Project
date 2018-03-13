package com.tecnico.sec.hds.server.db.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Function;

public class QueryHelpers {

  public static <A> A withTransaction(Function<Connection, A> query) throws DBException {
    try (Connection conn = connection()) {
      try {
        conn.setAutoCommit(false);
        A result = query.apply(conn);
        conn.commit();
        return result;
      } catch (SQLException e) {
        conn.rollback();
        throw new DBException(e.getMessage(), e);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException(e.getMessage(), e);
    }
  }

  public static <A> A withConnection(Function<Connection, A> query) throws DBException {
    try (Connection conn = connection()) {
      return query.apply(conn);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException(e.getMessage(), e);
    }
  }

  private static Connection connection() throws SQLException {
    String url = "jdbc:sqlite:HDSDB.db";
    return DriverManager.getConnection(url);
  }

}
