package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.AccountQueries;
import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChainTest {

  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    Optional<Transaction> t1opt = rules.sendAmount(createRandomAccount(), createRandomAccount(), 1, "a");
    Optional<Transaction> t2opt = rules.sendAmount(createRandomAccount(), createRandomAccount(), 2, "b");

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), t1.getSourceKey(), t1.getDestKey(),
        t1.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, t1.getSignature());

    String hash2 = new ChainHelper().generateTransactionHash(Optional.empty(), t2.getSourceKey(), t2.getDestKey(),
        t2.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, t2.getSignature());

    assertEquals(hash1, t1.getHash());
    assertEquals(hash2, t2.getHash());
  }

  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    String account = createRandomAccount();

    Optional<Transaction> t1opt = rules.sendAmount(account, createRandomAccount(), 1, "a");
    Optional<Transaction> t2opt = rules.sendAmount(account, createRandomAccount(), 2, "b");

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), t1.getSourceKey(), t1.getDestKey(),
        t1.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, t1.getSignature());

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(t1.getHash()), t2.getSourceKey(), t2.getDestKey(),
        t2.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, t2.getSignature());

    assertEquals(hash1, t1.getHash());
    assertEquals(hash2, t2.getHash());
  }


  private String createRandomAccount() throws DBException {
    return QueryHelpers.withConnection(conn -> {
      AccountQueries acc = new AccountQueries(conn);
      String key = randomPublicKey();
      acc.register(key);
      return key;
    });
  }

  // TODO change this for valid key
  private String randomPublicKey() {
    return UUID.randomUUID().toString();
  }
}
