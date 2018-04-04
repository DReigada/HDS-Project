package com.tecnico.sec.hds.server.db.commands;


import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.domain.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.tecnico.sec.hds.server.util.TestHelper.createRandomAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionQueriesTest {
  private static void assertTransaction(String sourceKey, String destKey, long amount,
                                        boolean pending, boolean isReceive, String signature, String hash, Transaction trans) {
    assertEquals(sourceKey, trans.sourceKey);
    assertEquals(destKey, trans.destKey);
    assertEquals(amount, trans.amount, 0);
    assertEquals(pending, trans.pending);
    assertEquals(isReceive, trans.receive);
    assertEquals(signature, trans.signature);
    assertEquals(hash, trans.hash);
  }

  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }

  @Test
  public void shouldInsertTransactionAndRetrieveIt() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      String acc1 = createRandomAccount();
      String acc2 = createRandomAccount();

      queries.insertNewTransaction(acc1, acc2, 1, false, true, "s", "h");
      Optional<Transaction> trans = queries.getLastInsertedTransaction();

      assertTrue(trans.isPresent());
      assertTransaction(acc1, acc2, 1, false, true, "s", "h", trans.get());

      return null;
    });
  }

  @Test
  public void shouldUpdateTransactionPendingState() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      String acc1 = createRandomAccount();
      String acc2 = createRandomAccount();

      queries.insertNewTransaction(acc1, acc2, 2, false, true, "s", "h");
      Optional<Transaction> trans = queries.getLastInsertedTransaction();
      assertTrue(trans.isPresent());


      queries.updateTransactionPendingState(trans.get().hash, true);

      Optional<Transaction> updatedTrans = queries.getTransactionByHash(trans.get().hash);
      assertTrue(updatedTrans.isPresent());

      assertTrue(updatedTrans.get().pending);

      return null;
    });
  }

  @Test
  public void shouldGetHistoryForOnlySendAmount() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      String mainAccount = createRandomAccount();
      String acc2 = createRandomAccount();
      String acc3 = createRandomAccount();

      queries.insertNewTransaction(mainAccount, acc2, 1, false, false, "s1", "1");
      queries.insertNewTransaction(mainAccount, acc2, 2, true, false, "s2", "2");
      queries.insertNewTransaction(mainAccount, acc3, 3, false, false, "s3", "3");

      List<Transaction> history = queries.getHistory(mainAccount, Optional.empty());

      assertEquals(3, history.size());

      assertTransaction(mainAccount, acc2, 1, false, false, "s1", "1", history.get(2));
      assertTransaction(mainAccount, acc2, 2, true, false, "s2", "2", history.get(1));
      assertTransaction(mainAccount, acc3, 3, false, false, "s3", "3", history.get(0));

      return null;
    });
  }

  @Test
  public void shouldGetHistoryForOnlyReceive() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      String mainAccount = createRandomAccount();
      String acc2 = createRandomAccount();
      String acc3 = createRandomAccount();

      queries.insertNewTransaction(acc2, mainAccount, 1, false, true, "s1", "1");
      queries.insertNewTransaction(acc2, mainAccount, 2, true, true, "s2", "2");
      queries.insertNewTransaction(acc3, mainAccount, 3, false, true, "s3", "3");

      List<Transaction> history = queries.getHistory(mainAccount, Optional.empty());

      assertEquals(3, history.size());

      assertTransaction(acc2, mainAccount, 1, false, true, "s1", "1", history.get(2));
      assertTransaction(acc2, mainAccount, 2, true, true, "s2", "2", history.get(1));
      assertTransaction(acc3, mainAccount, 3, false, true, "s3", "3", history.get(0));

      return null;
    });
  }

  @Test
  public void shouldGetHistoryForMixedTransaction() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      String mainAccount = createRandomAccount();
      String acc2 = createRandomAccount();
      String acc3 = createRandomAccount();

      queries.insertNewTransaction(mainAccount, acc2, 1, false, false, "s1", "1");
      queries.insertNewTransaction(mainAccount, acc2, 1, false, true, "s1", "1"); // Should not appear
      queries.insertNewTransaction(acc2, mainAccount, 2, true, true, "s2", "2");
      queries.insertNewTransaction(acc3, mainAccount, 3, false, true, "s3", "3");

      List<Transaction> history = queries.getHistory(mainAccount, Optional.empty());

      assertEquals(3, history.size());

      assertTransaction(mainAccount, acc2, 1, false, false, "s1", "1", history.get(2));
      assertTransaction(acc2, mainAccount, 2, true, true, "s2", "2", history.get(1));
      assertTransaction(acc3, mainAccount, 3, false, true, "s3", "3", history.get(0));

      return null;
    });
  }
}
