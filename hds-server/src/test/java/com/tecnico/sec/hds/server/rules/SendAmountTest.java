package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import domain.Transaction;
import com.tecnico.sec.hds.server.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static com.tecnico.sec.hds.server.util.TestHelper.createRandomAccount;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SendAmountTest {
  private QueryHelpers queryHelpers = new QueryHelpers();

  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate(new QueryHelpers());
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules(queryHelpers);

    Tuple<String, String> firstAccount = createRandomAccount();
    Tuple<String, String> firstDestAccount = createRandomAccount();
    Tuple<String, String> secondAccount = createRandomAccount();
    Tuple<String, String> secondDestAccount = createRandomAccount();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(firstAccount.second), Optional.empty(), firstAccount.first, firstDestAccount.first, 1,
        ChainHelper.TransactionType.SEND_AMOUNT);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(secondAccount.second), Optional.empty(), secondAccount.first, secondDestAccount.first, 2,
        ChainHelper.TransactionType.SEND_AMOUNT);

    Optional<Transaction> t1opt = rules.sendAmount(firstAccount.first, firstDestAccount.first, 1, "a", hash1);
    Optional<Transaction> t2opt = rules.sendAmount(secondAccount.first, secondDestAccount.first, 2, "b", hash2);

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
  }

  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules(queryHelpers);

    Tuple<String, String> account = createRandomAccount();
    Tuple<String, String> firstDest = createRandomAccount();
    Tuple<String, String> secondDest = createRandomAccount();


    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(account.second), Optional.empty(), account.first, firstDest.first,
        1, ChainHelper.TransactionType.SEND_AMOUNT);


    Optional<Transaction> t1opt = rules.sendAmount(account.first, firstDest.first, 1, "a", hash1);
    assertTrue(t1opt.isPresent());
    Transaction t1 = t1opt.get();


    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(t1.hash), Optional.empty(), account.first, secondDest.first,
        2, ChainHelper.TransactionType.SEND_AMOUNT);


    Optional<Transaction> t2opt = rules.sendAmount(account.first, secondDest.first, 2, "b", hash2);
    assertTrue(t2opt.isPresent());
    Transaction t2 = t2opt.get();

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
  }
}
