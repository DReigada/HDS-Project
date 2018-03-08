package resources.db.commands;

import domain.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class checkAmount extends amountCommand{


  public List<Transfer> getPendingTransfers(String publicKey){
    List<Transfer> pendingTransfers = new ArrayList<>();
    try(Connection conn = this.connection();
        PreparedStatement stmt = createPendingTransQuery(conn, publicKey);
        ResultSet rs = stmt.executeQuery()){

      while(rs.next()){
        pendingTransfers.add(new Transfer(rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getFloat(4), rs.getBoolean(5)));
      }


    } catch (SQLException e){
      e.printStackTrace();
    }
    return pendingTransfers;
  }

  public PreparedStatement createPendingTransQuery(Connection conn, String publicKey) throws SQLException{
    String query = "SELECT * FROM transfers WHERE destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

}
