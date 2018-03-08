package resources.db.commands;

import domain.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class checkAmount extends amountCommand{


  public List<Transfer> getPendingTransfers(String publicX, String publicY){
    List<Transfer> pendingTransfers = new ArrayList<>();
    try(Connection conn = this.connection();
        PreparedStatement stmt = createPendingTransQuery(conn, publicX, publicY);
        ResultSet rs = stmt.executeQuery()){

      while(rs.next()){
        pendingTransfers.add(new Transfer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                              rs.getString(5), rs.getFloat(6), rs.getBoolean(7)));
      }


    } catch (SQLException e){
      e.printStackTrace();
    }
    return pendingTransfers;
  }

  public PreparedStatement createPendingTransQuery(Connection conn, String publicX, String publicY) throws SQLException{
    String query = "SELECT * FROM transfers WHERE destX = ? AND destY = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicX);
    stmt.setString(2, publicY);
    return stmt;
  }

}
