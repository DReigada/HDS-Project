package com.tecnico.sec.hds.server.db.commands;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransferQueries {

  private Connection conn;

  public TransferQueries(Connection conn) {
    this.conn = conn;
  }

  public List<Transfer> getHistory(String publicKey) throws DBException {
    List<Transfer> history = new ArrayList<>();
    try (PreparedStatement stmt = createTransfersQuery(publicKey);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        history.add(new Transfer(rs.getInt(1), rs.getString(2), rs.getString(3),
          rs.getFloat(4), rs.getBoolean(5)));
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return history;
  }

  public PreparedStatement createTransfersQuery(String publicKey) throws SQLException {
    String query = "SELECT * FROM transfers WHERE destKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public List<Transfer> getPendingTransfers(String publicKey) throws DBException {
    List<Transfer> pendingTransfers = new ArrayList<>();
    try (PreparedStatement stmt = createPendingTransQuery(publicKey);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        pendingTransfers.add(new Transfer(rs.getInt(1), rs.getString(2), rs.getString(3),
          rs.getFloat(4), rs.getBoolean(5)));
      }


    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return pendingTransfers;
  }

  public PreparedStatement createPendingTransQuery(String publicKey) throws SQLException {
    String query = "SELECT * FROM transfers WHERE destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public float getTransAmount(int transID, String publicKey) throws DBException {
    float amount = 0;
    try (PreparedStatement stmt = createPendingTransQuery(transID, publicKey);
         ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        amount = rs.getFloat(1);
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e); // TODO change error message
    }
    return amount;
  }


  private PreparedStatement createPendingTransQuery(int transID, String publicKey) throws SQLException {
    String query = "SELECT amount FROM transfers WHERE transID = ? AND destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setInt(1, transID);
    stmt.setString(2, publicKey);
    return stmt;
  }


  public int updateTransfer(int transID) throws DBException {
    try (PreparedStatement stmt = createUpdateTransfer(transID)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }


  private PreparedStatement createUpdateTransfer(int transID) throws SQLException {
    String update = "UPDATE transfers SET pending = false WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setInt(1, transID);
    return stmt;
  }

  public int insertNewTransfer(String sourceKey, String destKey, float amount) throws DBException {
    try (PreparedStatement stmt = createInsertTransferStatment(sourceKey, destKey, amount)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createInsertTransferStatment(String sourcekey, String destKey, float amount) throws SQLException {
    String insert = "INSERT INTO transfers(sourceKey, destKey, amount, pending) VALUES (?, ?, ?, true)";
    PreparedStatement stmt = conn.prepareStatement(insert);
    stmt.setString(1, sourcekey);
    stmt.setString(2, destKey);
    stmt.setFloat(3, amount);
    return stmt;
  }

}