package resources.db.migration;


import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V1__Create_Tables implements JdbcMigration {

  @Override
  public void migrate(Connection connection) throws Exception {
    String accounts = "CREATE TABLE IF NOT EXISTS accounts (\n"
      + "publicX VARCHAR(500),\n"
      + "publicY VARCHAR(500),\n"
      + "counter INTEGER,\n"
      + "balance FLOAT,\n"
      + "PRIMARY KEY (publicX, publicY));";

    String transactions = "CREATE TABLE IF NOT EXISTS transactions (\n"
      + "transID INTEGER PRIMARY KEY,"
      + "sourceX VARCHAR(500),\n"
      + "sourceY VARCHAR(500),\n"
      + "destX VARCHAR(500),\n"
      + "destY VARCHAR(500),\n"
      + "amount FLOAT,\n"
      + "pending BOOL);";

    try(Statement stmt = connection.createStatement()){
      stmt.execute(accounts);
      stmt.execute(transactions);
    } catch (SQLException e){
      e.printStackTrace();
    }
  }
}

