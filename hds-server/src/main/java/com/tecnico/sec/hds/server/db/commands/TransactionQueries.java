package com.tecnico.sec.hds.server.db.commands;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionQueries {

  private Connection conn;

  public TransactionQueries(Connection conn) {
    this.conn = conn;
  }

  public List<Transaction> getHistory(String publicKey) throws DBException {
    List<Transaction> history = new ArrayList<>();
    try (PreparedStatement stmt = createTransactionsQuery(publicKey);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        history.add(new Transaction(rs.getInt(1), rs.getString(2), rs.getString(3),
          rs.getFloat(4), rs.getBoolean(5)));
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return history;
  }

  public PreparedStatement createTransactionsQuery(String publicKey) throws SQLException {
    String query = "SELECT * FROM transactions WHERE destKey = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public List<Transaction> getPendingTransactions(String publicKey) throws DBException {
    List<Transaction> pendingTransactions = new ArrayList<>();
    try (PreparedStatement stmt = createPendingTransQuery(publicKey);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        pendingTransactions.add(new Transaction(rs.getInt(1), rs.getString(2), rs.getString(3),
          rs.getFloat(4), rs.getBoolean(5)));
      }


    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return pendingTransactions;
  }

  public PreparedStatement createPendingTransQuery(String publicKey) throws SQLException {
    String query = "SELECT * FROM transactions WHERE destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }

  public float getTransAmount(int transID, String publicKey) throws DBException {
    try (PreparedStatement stmt = createPendingTransQuery(transID, publicKey);
         ResultSet rs = stmt.executeQuery()) {

      if (rs.next()) {
        return rs.getFloat(1);
      } else {
        throw new DBException("failed");
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e); // TODO change error message
    }
  }


  private PreparedStatement createPendingTransQuery(int transID, String publicKey) throws SQLException {
    String query = "SELECT amount FROM transactions WHERE transID = ? AND destKey = ? AND pendint = true";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setInt(1, transID);
    stmt.setString(2, publicKey);
    return stmt;
  }


  public int updateTransaction(int transID) throws DBException {
    try (PreparedStatement stmt = createUpdateTransaction(transID)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }


  private PreparedStatement createUpdateTransaction(int transID) throws SQLException {
    String update = "UPDATE transactions SET pending = false WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setInt(1, transID);
    return stmt;
  }

  public int insertNewTransaction(String sourceKey, String destKey, float amount,
                                  boolean pending, String signature, String hash) throws DBException {
    try (PreparedStatement stmt = createInsertTransactionStatment(sourceKey, destKey, amount, pending, signature, hash)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createInsertTransactionStatment(String sourcekey, String destKey, float amount,
                                                           boolean pending, String signature, String hash) throws SQLException {
    String insert = "INSERT INTO transactions(sourceKey, destKey, amount, pending, signature, hash) VALUES (?, ?, ?, ?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(insert);
    stmt.setString(1, sourcekey);
    stmt.setString(2, destKey);
    stmt.setFloat(3, amount);
    stmt.setBoolean(4, pending);
    stmt.setString(5, signature);
    stmt.setString(6, hash);
    return stmt;
  }

}
