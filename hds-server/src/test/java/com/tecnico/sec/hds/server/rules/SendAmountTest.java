package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.util.Migrations;
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

  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    Tuple<String, String> firstAccount = createRandomAccount();
    Tuple<String, String> secondAccount = createRandomAccount();

    Optional<Transaction> t1opt = rules.sendAmount(firstAccount.first, createRandomAccount().first, 1, "a", firstAccount.second);
    Optional<Transaction> t2opt = rules.sendAmount(secondAccount.first, createRandomAccount().first, 2, "b", secondAccount.second);

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(firstAccount.second), Optional.empty(),t1.sourceKey, t1.destKey,
        t1.amount, ChainHelper.TransactionType.SEND_AMOUNT, t1.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(secondAccount.second), Optional.empty(),t2.sourceKey, t2.destKey,
        t2.amount, ChainHelper.TransactionType.SEND_AMOUNT, t2.signature);

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
  }

  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    Tuple<String, String> account = createRandomAccount();

    Optional<Transaction> t1opt = rules.sendAmount(account.first, createRandomAccount().first, 1, "a", account.second);
    assertTrue(t1opt.isPresent());
    Transaction t1 = t1opt.get();

    Optional<Transaction> t2opt = rules.sendAmount(account.first, createRandomAccount().first, 2, "b", t1opt.get().hash);
    assertTrue(t2opt.isPresent());
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(account.second), Optional.empty(),t1.sourceKey, t1.destKey,
        t1.amount, ChainHelper.TransactionType.SEND_AMOUNT, t1.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(t1.hash), Optional.empty(),t2.sourceKey, t2.destKey,
        t2.amount, ChainHelper.TransactionType.SEND_AMOUNT, t2.signature);

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
  }
}
