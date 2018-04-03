package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static com.tecnico.sec.hds.server.util.TestHelper.createRandomAccount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReceiveAmountTest {
  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(createRandomAccount(), createRandomAccount(), 1, "a");

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), sendAmountTrans.getSourceKey(), sendAmountTrans.getDestKey(),
        sendAmountTrans.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.getSignature());

    String hash2 = new ChainHelper().generateTransactionHash(Optional.empty(), receiveAmountTrans.getSourceKey(), receiveAmountTrans.getDestKey(),
        receiveAmountTrans.getAmount(), ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.getSignature());

    assertEquals(hash1, sendAmountTrans.getHash());
    assertEquals(hash2, receiveAmountTrans.getHash());
  }


  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    String destAcc = createRandomAccount();

    Optional<Transaction> previousTransOpt = sendAmountRules.sendAmount(destAcc, createRandomAccount(), 1, "a");
    assertTrue(previousTransOpt.isPresent());

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(createRandomAccount(), destAcc, 1, "a");

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), sendAmountTrans.getSourceKey(), sendAmountTrans.getDestKey(),
        sendAmountTrans.getAmount(), ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.getSignature());

    String hash2 = new ChainHelper().generateTransactionHash(previousTransOpt.map(Transaction::getHash),
        receiveAmountTrans.getSourceKey(), receiveAmountTrans.getDestKey(),
        receiveAmountTrans.getAmount(), ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.getSignature());

    assertEquals(hash1, sendAmountTrans.getHash());
    assertEquals(hash2, receiveAmountTrans.getHash());
  }
}
