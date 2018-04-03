package com.tecnico.sec.hds.server.db.commands;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.domain.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionQueries {

  private Connection conn;

  public TransactionQueries(Connection conn) {
    this.conn = conn;
  }

  public Optional<Transaction> getLastTransaction(String publicKey) throws DBException {
    List<Transaction> list = getHistory(publicKey, Optional.of(1));

    if (list.size() > 0) {
      return Optional.of(list.get(0));
    } else {
      return Optional.empty();
    }
  }

  public List<Transaction> getHistory(String publicKey, Optional<Integer> limit) throws DBException {
    List<Transaction> history = new ArrayList<>();
    try (PreparedStatement stmt = listTransactionsQuery(publicKey, limit);
         ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        history.add(createTransactionFromResultSet(rs));
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return history;
  }

  private PreparedStatement listTransactionsQuery(String publicKey, Optional<Integer> limit) throws SQLException {
    String bla = limit.map(integer -> " LIMIT " + integer).orElse("");
    String query =
        "SELECT * FROM transactions" +
            " WHERE (sourceKey = ? AND receive) OR (destKey = ? AND NOT receive)" +
            " ORDER BY transID DESC " + bla;
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    stmt.setString(2, publicKey);
    return stmt;
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
        pendingTransactions.add(createTransactionFromResultSet(rs));
      }


    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
    return pendingTransactions;
  }

  public PreparedStatement createPendingTransQuery(String publicKey) throws SQLException {
    String query = "SELECT * FROM transactions WHERE destKey = ? AND pendint = TRUE";
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
    String query = "SELECT amount FROM transactions WHERE transID = ? AND destKey = ? AND pendint = TRUE";
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
    String update = "UPDATE transactions SET pending = FALSE WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setInt(1, transID);
    return stmt;
  }

  public int insertNewTransaction(String sourceKey, String destKey, float amount,
                                  boolean isReceive, String signature, String hash) throws DBException {
    try (PreparedStatement stmt = createInsertTransactionStatment(sourceKey, destKey, amount, isReceive, signature, hash)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createInsertTransactionStatment(String sourcekey, String destKey, float amount,
                                                           boolean isReceive, String signature, String hash) throws SQLException {
    String insert = "INSERT INTO transactions(sourceKey, destKey, amount, receive, signature, hash) VALUES (?, ?, ?, ?, ?, ?)";
    PreparedStatement stmt = conn.prepareStatement(insert);
    stmt.setString(1, sourcekey);
    stmt.setString(2, destKey);
    stmt.setFloat(3, amount);
    stmt.setBoolean(4, isReceive);
    stmt.setString(5, signature);
    stmt.setString(6, hash);
    return stmt;
  }


  public Optional<Transaction> getTransactionByHash(String hash) throws DBException {
    try (PreparedStatement stmt = createGetTransactionByHashQuery(hash);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return Optional.of(createTransactionFromResultSet(rs));
      } else {
        return Optional.empty();
      }

    } catch (SQLException e) {
      throw new DBException("some error", e);
    }
  }

  private PreparedStatement createGetTransactionByHashQuery(String hash) throws SQLException {
    String query = "SELECT transID, sourceKey, destKey, amount, receive, signature, hash FROM transactions WHERE hash = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, hash);
    return stmt;
  }

  public Optional<Transaction> getTransactionById(int id) throws DBException {
    try (PreparedStatement stmt = createGetTransactionByIDQuery(id);
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return Optional.of(createTransactionFromResultSet(rs));
      } else {
        return Optional.empty();
      }

    } catch (SQLException e) {
      throw new DBException("some error", e);
    }
  }

  private PreparedStatement createGetTransactionByIDQuery(int id) throws SQLException {
    String query = "SELECT transID, sourceKey, destKey, amount, receive, signature, hash FROM transactions WHERE transID = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setInt(1, id);
    return stmt;
  }

  public Optional<Transaction> getLastInsertedTransaction() throws DBException {
    try (PreparedStatement stmt = createGetLastInsertedTransactionQuery();
         ResultSet rs = stmt.executeQuery()) {
      if (rs.next()) {
        return Optional.of(createTransactionFromResultSet(rs));
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DBException("some error", e);
    }

  }

  private PreparedStatement createGetLastInsertedTransactionQuery() throws SQLException {
    String query =
        "SELECT transID, sourceKey, destKey, amount, receive, signature, hash" +
            " FROM transactions" +
            " WHERE transID = (SELECT LAST_INSERT_ID())";

    return conn.prepareStatement(query);
  }

  private Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
    return new Transaction(rs.getInt(1), rs.getString(2), rs.getString(3),
        rs.getFloat(4), rs.getBoolean(5), rs.getString(6), rs.getString(7));
  }

}
