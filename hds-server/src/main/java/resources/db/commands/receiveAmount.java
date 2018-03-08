package resources.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class receiveAmount extends amountCommand{

  public boolean receive(int transID, String publicX, String publicY){
    float amount = getTransAmount(transID, publicX, publicY);
    float balance = getBalance(publicX, publicY);
    if (amount < 0 && balance != -1){
      boolean updateTransfer = updateTransfer(transID);
      boolean updateAccount = updateAccount(publicX, publicY, amount + balance);
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

  public boolean updateAccount(String publicX, String publicY, float balance){
      try(Connection conn = this.connection();
        PreparedStatement stmt = createUpdateAccount(conn, publicX, publicY, balance)){

        int result = stmt.executeUpdate();

        if (result == 1){
          return true;
        }

      } catch (SQLException e){
        e.printStackTrace();
      }
      return false;
  }


  public float getTransAmount(int transID, String publicX, String publicY){
    float amount = 0;
    try(Connection conn = this.connection();
        PreparedStatement stmt = createPendingTransQuery(conn, transID, publicX, publicY);
        ResultSet rs = stmt.executeQuery()){

      if(rs.next()){
        amount = rs.getFloat(1);
      }

    } catch (SQLException e){
      e.printStackTrace();
    }
    return amount;
  }


  private PreparedStatement createPendingTransQuery(Connection conn, int transID, String publicX, String publicY) throws SQLException {
    String query = "SELECT amount FROM transfers WHERE transID = ? AND destX = ? AND destY = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setInt(1, transID);
    stmt.setString(2, publicX);
    stmt.setString(3, publicY);
    return stmt;
  }

  private PreparedStatement createUpdateTransfer(Connection conn, int transID) throws SQLException{
    String update = "UPDATE transfers SET pending = false WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setInt(1, transID);
    return stmt;
  }

  private PreparedStatement createUpdateAccount(Connection conn, String publicX, String publicY, float balance) throws SQLException{
    String update = "UPDATE accounts SET balance = ? WHERE publicX = ? AND publicY = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setFloat(1, balance);
    stmt.setString(2, publicX);
    stmt.setString(3, publicY);
    return stmt;
  }
}
