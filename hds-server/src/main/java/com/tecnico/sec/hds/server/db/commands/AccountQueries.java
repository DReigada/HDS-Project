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

  public long getBalance(String publicKey) throws DBException {
    try (PreparedStatement stmt = createBalanceQuery(publicKey);
         ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        return rs.getLong(1);
      } else {
        throw new DBException("Account not found!"); // TODO maybe return optional?
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createBalanceQuery(String publicKey) throws SQLException {
    String query = "SELECT balance FROM accounts WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public int updateAccount(String publicKey, long balance) throws DBException {
    try (PreparedStatement stmt = createUpdateAccount(publicKey, balance)) {

      return stmt.executeUpdate();


    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createUpdateAccount(String publicKey, long balance) throws SQLException {
    String update = "UPDATE accounts SET balance = ? WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setLong(1, balance);
    stmt.setString(2, publicKey);
    return stmt;
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
    String sql = "INSERT INTO accounts(publicKey, balance) VALUES(?, 1000) ";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public int remove(String publicKey){
    try (PreparedStatement stmt = removeAccount(publicKey)) {
      return stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private PreparedStatement removeAccount(String publicKey) throws SQLException{
    String sql = "DELETE FROM accounts WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    return stmt;
  }

}
