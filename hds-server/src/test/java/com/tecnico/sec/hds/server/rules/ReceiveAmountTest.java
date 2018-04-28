package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
import com.tecnico.sec.hds.server.db.rules.SendAmountRules;
import com.tecnico.sec.hds.server.domain.Transaction;
import com.tecnico.sec.hds.server.util.Tuple;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static com.tecnico.sec.hds.server.util.TestHelper.createRandomAccount;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ReceiveAmountTest {
  @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    Tuple<String, String> account = createRandomAccount();
    Tuple<String, String> destAcc = createRandomAccount();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(account.first, destAcc.first, 1, "a", account.second);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, destAcc.second, "qwerty");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(account.second), Optional.empty(), sendAmountTrans.sourceKey, sendAmountTrans.destKey,
      sendAmountTrans.amount, ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(destAcc.second), Optional.of(sendAmountTrans.hash), receiveAmountTrans.sourceKey, receiveAmountTrans.destKey,
      receiveAmountTrans.amount, ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.signature);

    assertEquals(hash1, sendAmountTrans.hash);
    assertEquals(hash2, receiveAmountTrans.hash);
  }


  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    Tuple<String, String> sendAcc = createRandomAccount();
    
    Tuple<String, String> destAcc = createRandomAccount();

    Optional<Transaction> previousTransOpt = sendAmountRules.sendAmount(destAcc.first, createRandomAccount().first, 1, "a", destAcc.second);
    assertTrue(previousTransOpt.isPresent());

    Tuple<String, String> oneAccount = createRandomAccount();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(oneAccount.first, destAcc.first, 1, "a", oneAccount.second);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, previousTransOpt.get().hash, "qwerty");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(oneAccount.second), Optional.empty(),sendAmountTrans.sourceKey, sendAmountTrans.destKey,
      sendAmountTrans.amount, ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.signature);

    String hash2 = new ChainHelper().generateTransactionHash(previousTransOpt.map(t -> t.hash), Optional.of(hash1),
      receiveAmountTrans.sourceKey, receiveAmountTrans.destKey,
      receiveAmountTrans.amount, ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.signature);

    assertEquals(hash1, sendAmountTrans.hash);
    assertEquals(hash2, receiveAmountTrans.hash);
  }

  @Test
  public void shouldNotAcceptTheSameTransactionMoreThanOnce() throws DBException {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    Tuple<String, String> sourceAcc = createRandomAccount();

    Tuple<String, String> destAcc = createRandomAccount();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(sourceAcc.first, destAcc.first, 1, "a", sourceAcc.second);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt1 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, destAcc.second, "qwerty");
    Optional<Transaction> receiveAmountTransOpt2 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, destAcc.second, "qwerty");

    assertTrue(receiveAmountTransOpt1.isPresent());
    assertFalse(receiveAmountTransOpt2.isPresent());
  }

}
