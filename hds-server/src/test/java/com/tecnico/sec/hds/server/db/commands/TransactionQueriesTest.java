package com.tecnico.sec.hds.server.db.commands;


import com.tecnico.sec.hds.server.db.commands.TransactionQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.domain.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static com.tecnico.sec.hds.server.util.TestHelper.createRandomAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionQueriesTest {
  private static void assertTransaction(Transaction trans, String sourceKey, String destKey, float amount,
                                        boolean isReceive, String signature, String hash) {
    assertEquals(trans.sourceKey, sourceKey);
    assertEquals(trans.destKey, destKey);
    assertEquals(trans.amount, amount, 0);
    assertEquals(trans.pending, isReceive);
    assertEquals(trans.signature, signature);
    assertEquals(trans.hash, hash);
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

      queries.insertNewTransaction(acc1, acc2, 1.1f, true, "s", "h");
      Optional<Transaction> trans = queries.getLastInsertedTransaction();

      assertTrue(trans.isPresent());
      assertTransaction(trans.get(), acc1, acc2, 1.1f, true, "s", "h");

      return null;
    });
  }

}
