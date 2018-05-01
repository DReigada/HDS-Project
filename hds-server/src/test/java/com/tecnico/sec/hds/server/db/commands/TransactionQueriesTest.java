package com.tecnico.sec.hds.server.db.commands;


import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.domain.Transaction;

import com.tecnico.sec.hds.server.util.Tuple;
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
    assertEquals(amount, trans.amount);
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

      Tuple<String, String> acc1 = createRandomAccount();
      Tuple<String, String> acc2 = createRandomAccount();

      queries.insertNewTransaction(acc1.first, acc2.first, 1, false, true, "s", acc1.second, Optional.empty());
      Optional<Transaction> trans = queries.getLastInsertedTransaction();

      assertTrue(trans.isPresent());
      assertTransaction(acc1.first, acc2.first, 1, false, true, "s", acc1.second, trans.get());

      return null;
    });
  }

  @Test
  public void shouldUpdateTransactionPendingState() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      Tuple<String, String> acc1 = createRandomAccount();
      Tuple<String, String> acc2 = createRandomAccount();

      queries.insertNewTransaction(acc1.first, acc2.first, 2, false, true, "s", acc1.second, Optional.empty());
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

      Tuple<String, String> mainAccount = createRandomAccount();
      Tuple<String, String> acc2 = createRandomAccount();
      Tuple<String, String> acc3 = createRandomAccount();

      queries.insertNewTransaction(mainAccount.first, acc2.first, 1, false, false, "s1", "1", Optional.empty());
      queries.insertNewTransaction(mainAccount.first, acc2.first, 2, true, false, "s2", "2", Optional.empty());
      queries.insertNewTransaction(mainAccount.first, acc3.first, 3, false, false, "s3", "3", Optional.empty());

      List<Transaction> history = queries.getHistory(mainAccount.first, Optional.empty());

      assertEquals(4, history.size());

      assertTransaction(mainAccount.first, acc2.first, 1, false, false, "s1", "1", history.get(2));
      assertTransaction(mainAccount.first, acc2.first, 2, true, false, "s2", "2", history.get(1));
      assertTransaction(mainAccount.first, acc3.first, 3, false, false, "s3", "3", history.get(0));

      return null;
    });
  }

  @Test
  public void shouldGetHistoryForOnlyReceive() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      Tuple<String, String> mainAccount = createRandomAccount();
      Tuple<String, String> acc2 = createRandomAccount();
      Tuple<String, String> acc3 = createRandomAccount();

      queries.insertNewTransaction(acc2.first, mainAccount.first, 1, false, true, "s1", "1", Optional.empty());
      queries.insertNewTransaction(acc2.first, mainAccount.first, 2, true, true, "s2", "2", Optional.empty());
      queries.insertNewTransaction(acc3.first, mainAccount.first, 3, false, true, "s3", "3", Optional.empty());

      List<Transaction> history = queries.getHistory(mainAccount.first, Optional.empty());

      assertEquals(4, history.size());

      assertTransaction(acc2.first, mainAccount.first, 1, false, true, "s1", "1", history.get(2));
      assertTransaction(acc2.first, mainAccount.first, 2, true, true, "s2", "2", history.get(1));
      assertTransaction(acc3.first, mainAccount.first, 3, false, true, "s3", "3", history.get(0));

      return null;
    });
  }

  @Test
  public void shouldGetHistoryForMixedTransaction() throws DBException {
    QueryHelpers.withConnection(conn -> {
      TransactionQueries queries = new TransactionQueries(conn);

      Tuple<String, String> mainAccount = createRandomAccount();
      Tuple<String, String> acc2 = createRandomAccount();
      Tuple<String, String> acc3 = createRandomAccount();

      queries.insertNewTransaction(mainAccount.first, acc2.first, 1, false, false, "s1", "1", Optional.empty());
      queries.insertNewTransaction(mainAccount.first, acc2.first, 1, false, true, "s1", "1", Optional.empty()); // Should not appear
      queries.insertNewTransaction(acc2.first, mainAccount.first, 2, true, true, "s2", "2", Optional.empty());
      queries.insertNewTransaction(acc3.first, mainAccount.first, 3, false, true, "s3", "3", Optional.empty());

      List<Transaction> history = queries.getHistory(mainAccount.first, Optional.empty());

      assertEquals(4, history.size());

      assertTransaction(mainAccount.first, acc2.first, 1, false, false, "s1", "1", history.get(2));
      assertTransaction(acc2.first, mainAccount.first, 2, true, true, "s2", "2", history.get(1));
      assertTransaction(acc3.first, mainAccount.first, 3, false, true, "s3", "3", history.get(0));

      return null;
    });
  }
}
