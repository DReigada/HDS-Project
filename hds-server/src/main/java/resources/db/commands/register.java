package resources.db.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class register extends dbCommand {

  public boolean register(String publicX, String publicY){
    if(!checkKey(publicX, publicY)) {
      return false;
    }
    try (Connection conn = this.connection();
         PreparedStatement stmt = createInsert(conn, publicX, publicY)) {
      int result = stmt.executeUpdate();
      if (result == 1) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean checkKey(String publicX, String publicY){
    try(Connection conn = this.connection();
        PreparedStatement stmt = createQuery(conn, publicX, publicY);
        ResultSet rs = stmt.executeQuery()){

      if (rs.next()) {
        return false;
      }

    } catch(SQLException e){
      e.printStackTrace();
    }
    return true;
  }

  public PreparedStatement createQuery(Connection conn, String publicX, String publicY) throws SQLException{
    String query = "SELECT publicX, publicY FROM accounts WHERE publicX = ?, publicY = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicX);
    stmt.setString(2, publicY);
    return stmt;
  }

  public PreparedStatement createInsert(Connection conn, String publicX, String publicY) throws SQLException{
    String sql = "INSERT INTO accounts(publicX, publicY, counter, balance) VALUES(?, ?, 1, 1000) ";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicX);
    stmt.setString(2, publicY);
    return stmt;
  }
}
