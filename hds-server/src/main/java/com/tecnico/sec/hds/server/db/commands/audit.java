package com.tecnico.sec.hds.server.db.commands;

import com.tecnico.sec.hds.server.domain.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class audit extends dbCommand{

  public List<Transfer> getHistory(String publicKey){
    List<Transfer> history = new ArrayList<>();
    try(Connection conn = this.connection();
        PreparedStatement stmt = createTransfersQuery(conn, publicKey);
        ResultSet rs = stmt.executeQuery()) {

      while(rs.next()){
        history.add(new Transfer(rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getFloat(4), rs.getBoolean(5)));
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return history;
  }

  public PreparedStatement createTransfersQuery(Connection conn, String publicKey) throws SQLException {
    String query = "SELECT * FROM transfers WHERE destKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

}
