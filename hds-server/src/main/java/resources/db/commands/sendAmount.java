package resources.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class sendAmount extends amountCommand{

  public boolean send(String sourceKey, String destKey, float amount){
    float sourceBalance = getBalance(sourceKey);
    if (amount <= sourceBalance){
      boolean accountUpdated = updateAccount(sourceKey, sourceBalance - amount);
      boolean transferInserted = insertNewTransfer(sourceKey, destKey, amount);
      return (accountUpdated && transferInserted);
    }
    return false;
  }


  public boolean insertNewTransfer(String sourceKey, String destKey, float amount){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createInsertTransferStatment(conn, sourceKey, destKey, amount)){

      int result = stmt.executeUpdate();

      if(result == 1){
        return true;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public PreparedStatement createInsertTransferStatment(Connection conn, String sourcekey, String destKey, float amount) throws SQLException{
    String insert = "INSERT INTO transfers(sourceKey, destKey, amount, pending) VALUES (?, ?, ?, true)";
    PreparedStatement stmt = conn.prepareStatement(insert);
    stmt.setString(1, sourcekey);
    stmt.setString(2, destKey);
    stmt.setFloat(3, amount);
    return stmt;
  }

}
