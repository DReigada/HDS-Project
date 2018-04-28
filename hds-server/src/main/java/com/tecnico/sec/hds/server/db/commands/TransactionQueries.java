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

  public long getBalance(String publicKey) throws DBException {
    List<Transaction> history = getHistory(publicKey, Optional.empty());
    long amount = history.stream().mapToLong(t -> t.receive ? t.amount : -t.amount).sum();

    return amount;
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
        " WHERE (sourceKey = ? AND NOT receive) OR (destKey = ? AND receive)" +
        " ORDER BY transID DESC " + bla;
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    stmt.setString(2, publicKey);
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
    String query = "SELECT * FROM transactions WHERE destKey = ? AND pending = TRUE";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, publicKey);
    return stmt;
  }


  public int updateTransactionPendingState(String transHash, boolean pending) throws DBException {
    try (PreparedStatement stmt = createUpdateTransactionPendingState(transHash, pending)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }


  private PreparedStatement createUpdateTransactionPendingState(String transHash, boolean pending) throws SQLException {
    String update = "UPDATE transactions SET pending = ? WHERE HASH = ?";
    PreparedStatement stmt = conn.prepareStatement(update);
    stmt.setBoolean(1, pending);
    stmt.setString(2, transHash);
    return stmt;
  }

  public int insertNewTransaction(String sourceKey, String destKey, long amount,
                                  boolean pending, boolean isReceive, String signature, String hash, Optional<String> receiveHash) throws DBException {
    try (PreparedStatement stmt = createInsertTransactionStatment(sourceKey, destKey, amount, pending, isReceive, signature, hash, receiveHash)) {

      return stmt.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("some error", e);
    }
  }

  public PreparedStatement createInsertTransactionStatment(String sourcekey, String destKey, long amount, boolean pending,
                                                           boolean isReceive, String signature, String hash, Optional<String> receiveHash) throws SQLException {
    String insert = "INSERT INTO transactions(sourceKey, destKey, amount, pending, receive, signature, hash, receive_hash) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
    PreparedStatement stmt = conn.prepareStatement(insert);
    stmt.setString(1, sourcekey);
    stmt.setString(2, destKey);
    stmt.setLong(3, amount);
    stmt.setBoolean(4, pending);
    stmt.setBoolean(5, isReceive);
    stmt.setString(6, signature);
    stmt.setString(7, hash);
    stmt.setString(8, receiveHash.orElse(""));
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
    String query = "SELECT transID, sourceKey, destKey, amount, pending, receive, signature, hash FROM transactions WHERE hash = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, hash);
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
      "SELECT transID, sourceKey, destKey, amount, pending, receive, signature, hash" +
        " FROM transactions" +
        " WHERE transID = (SELECT LAST_INSERT_ID())";

    return conn.prepareStatement(query);
  }

  private Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
    return new Transaction(rs.getInt(1), rs.getString(2), rs.getString(3),
      rs.getLong(4), rs.getBoolean(5), rs.getBoolean(6), rs.getString(7), rs.getString(8));
  }

  public int removeAllTransactions(String publicKey) {
    try (PreparedStatement stmt = removeTransaction(publicKey)) {
      return stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  private PreparedStatement removeTransaction(String publicKey) throws SQLException {
    String sql = "DELETE FROM transactions WHERE sourceKey = ? OR destKey = ?";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setString(1, publicKey);
    stmt.setString(2, publicKey);
    return stmt;
  }

}
