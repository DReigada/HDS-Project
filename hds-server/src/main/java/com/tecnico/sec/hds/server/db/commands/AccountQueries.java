package com.tecnico.sec.hds.server.db.commands;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountQueries {

  private Connection conn;

  public AccountQueries(Connection conn) {
    this.conn = conn;
  }

  public int register(String publicKey) throws DBException {
    try (PreparedStatement stmt = createInsertAccount(publicKey)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }


  public PreparedStatement createInsertAccount(String publicKey) throws SQLException {
    String sql = "INSERT INTO accounts(publicKey) VALUES(?) ";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public int remove(String publicKey) {
    try (PreparedStatement stmt = removeAccount(publicKey)) {
      return stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private PreparedStatement removeAccount(String publicKey) throws SQLException {
    String sql = "DELETE FROM accounts WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    return stmt;
  }

}
