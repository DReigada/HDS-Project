package com.tecnico.sec.hds.server.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class receiveAmount extends amountCommand{

  public boolean receive(int transID, String publicKey){
    float amount = getTransAmount(transID, publicKey);
    float balance = getBalance(publicKey);
    if (amount < 0 && balance != -1){
      boolean updateTransfer = updateTransfer(transID);
      boolean updateAccount = updateAccount(publicKey, amount + balance);
      return (updateAccount && updateTransfer);
    }
    return false;
  }

  public boolean updateTransfer(int transID){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createUpdateTransfer(conn, transID)){

      int result = stmt.executeUpdate();

      if (result == 1){
        return true;
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return false;
  }



  public float getTransAmount(int transID, String publicKey){
    float amount = 0;
    try(Connection conn = this.connection();
        PreparedStatement stmt = createPendingTransQuery(conn, transID, publicKey);
        ResultSet rs = stmt.executeQuery()){

      if(rs.next()){
        amount = rs.getFloat(1);
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return amount;
  }


  private PreparedStatement createPendingTransQuery(Connection conn, int transID, String publicKey) throws SQLException {
    String query = "SELECT amount FROM transfers WHERE transID = ? AND destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setInt(1, transID);
    stmt.setString(2, publicKey);
    return stmt;
  }

  private PreparedStatement createUpdateTransfer(Connection conn, int transID) throws SQLException{
    String update = "UPDATE transfers SET pending = false WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setInt(1, transID);
    return stmt;
  }

}
