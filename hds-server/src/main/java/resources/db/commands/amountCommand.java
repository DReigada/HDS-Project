package resources.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class amountCommand extends dbCommand{

  public float getBalance(String publicX, String publicY){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createBalanceQuery(conn, publicX, publicY);
        ResultSet rs = stmt.executeQuery()){

      if(rs.next()){
        return rs.getFloat("balance");
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return -1;
  }

  //FIXME: refactor
  public PreparedStatement createBalanceQuery(Connection conn, String publicX, String publicY) throws SQLException{
    String query = "SELECT balance FROM accounts WHERE publicX = ? AND publicY = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicX);
    stmt.setString(2, publicY);
    return stmt;
  }

}
