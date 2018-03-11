package com.tecnico.sec.hds.server.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class amountCommand extends dbCommand{

  public float getBalance(String publicKey){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createBalanceQuery(conn, publicKey);
        ResultSet rs = stmt.executeQuery()){

      if(rs.next()){
        return rs.getFloat("balance");
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return -1;
  }


  public PreparedStatement createBalanceQuery(Connection conn, String publicKey) throws SQLException{
    String query = "SELECT balance FROM accounts WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public boolean updateAccount(String publicKey, float balance){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createUpdateAccount(conn, publicKey, balance)){

      int result = stmt.executeUpdate();

      if (result == 1){
        return true;
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return false;
  }

  public PreparedStatement createUpdateAccount(Connection conn, String publicKey, float balance) throws SQLException{
    String update = "UPDATE accounts SET balance = ? WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setFloat(1, balance);
    stmt.setString(2, publicKey);
    return stmt;
  }
}
