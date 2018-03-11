package com.tecnico.sec.hds.server.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class register extends dbCommand {

  public boolean run(String publicKey){
    if(!checkKey(publicKey)) {
      return false;
    }
    try (Connection conn = this.connection();
         PreparedStatement stmt = createInsert(conn, publicKey)) {
      int result = stmt.executeUpdate();
      if (result == 1) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean checkKey(String publicKey){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createQuery(conn, publicKey);
        ResultSet rs = stmt.executeQuery()){

      if (rs.next()) {
        return false;
      }

    } catch(SQLException e){
      e.printStackTrace();
    }
    return true;
  }


  public PreparedStatement createQuery(Connection conn, String publicKey) throws SQLException{
    String query = "SELECT publicKey FROM accounts WHERE publicKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public PreparedStatement createInsert(Connection conn, String publicKey) throws SQLException{
    String sql = "INSERT INTO accounts(publicKey, counter, balance) VALUES(?, 1, 1000) ";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    return stmt;
  }
}
