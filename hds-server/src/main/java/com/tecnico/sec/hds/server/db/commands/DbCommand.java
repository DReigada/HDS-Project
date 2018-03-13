package com.tecnico.sec.hds.server.db.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbCommand {

  protected Connection connection(){
    String url = "jdbc:sqlite:HDSDB.db";
    Connection conn = null;
    try{
      conn = DriverManager.getConnection(url);
    } catch (SQLException e){
      e.printStackTrace();
    }
    return conn;
  }
}
