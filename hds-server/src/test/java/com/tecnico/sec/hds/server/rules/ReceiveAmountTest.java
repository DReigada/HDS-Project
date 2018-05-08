package com.tecnico.sec.hds.server.rules;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;
import com.tecnico.sec.hds.server.db.commands.util.Migrations;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.ReceiveAmountRules;
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
import static org.junit.Assert.assertFalse;

public class ReceiveAmountTest {
  private QueryHelpers queryHelpers = new QueryHelpers();

  @BeforeClass
  public static void beforeClass() {
        Migrations.migrate(new QueryHelpers());
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules(queryHelpers);
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules(queryHelpers);

    Tuple<String, String> account = createRandomAccount();
    Tuple<String, String> destAcc = createRandomAccount();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(account.second), Optional.empty(), account.first, destAcc.first,
        1, ChainHelper.TransactionType.SEND_AMOUNT);

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(account.first, destAcc.first, 1, "a", hash1);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(destAcc.second), Optional.of(sendAmountTrans.hash), account.first, destAcc.first,
        sendAmountTrans.amount, ChainHelper.TransactionType.ACCEPT);


    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, hash2, "qwerty");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();


    assertEquals(hash1, sendAmountTrans.hash);
    assertEquals(hash2, receiveAmountTrans.hash);
  }


  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules(queryHelpers);
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules(queryHelpers);

    Tuple<String, String> sendAcc = createRandomAccount();
    
    Tuple<String, String> destAcc = createRandomAccount();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(destAcc.second), Optional.empty(), destAcc.first, sendAcc.first,
        1, ChainHelper.TransactionType.SEND_AMOUNT);


    Optional<Transaction> previousTransOpt = sendAmountRules.sendAmount(destAcc.first, sendAcc.first, 1, "a", hash1);
    assertTrue(previousTransOpt.isPresent());

    Tuple<String, String> oneAccount = createRandomAccount();

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(oneAccount.second), Optional.empty(), oneAccount.first, destAcc.first,
        1, ChainHelper.TransactionType.SEND_AMOUNT);


    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(oneAccount.first, destAcc.first, 1, "a", hash2);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();


    String hash3 = new ChainHelper().generateTransactionHash(previousTransOpt.map(t -> t.hash), Optional.of(hash2),
        sendAmountTrans.sourceKey, sendAmountTrans.destKey,
        sendAmountTrans.amount, ChainHelper.TransactionType.ACCEPT);

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, hash3, "qwerty");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();


    assertEquals(hash1, previousTransOpt.get().hash);
    assertEquals(hash2, sendAmountTrans.hash);
    assertEquals(hash3, receiveAmountTrans.hash);
  }

  @Test
  public void shouldNotAcceptTheSameTransactionMoreThanOnce() throws DBException {
    SendAmountRules sendAmountRules = new SendAmountRules(queryHelpers);
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules(queryHelpers);

    Tuple<String, String> sourceAcc = createRandomAccount();

    Tuple<String, String> destAcc = createRandomAccount();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.of(sourceAcc.second), Optional.empty(), sourceAcc.first, destAcc.first,
        1, ChainHelper.TransactionType.SEND_AMOUNT);

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(sourceAcc.first, destAcc.first, 1, "a", hash1);

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(destAcc.second), Optional.of(sendAmountTrans.hash), sourceAcc.first, destAcc.first,
        1, ChainHelper.TransactionType.ACCEPT);

    Optional<Transaction> receiveAmountTransOpt1 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, hash2, "qwerty");
    Optional<Transaction> receiveAmountTransOpt2 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, sendAmountTrans.sourceKey, sendAmountTrans.destKey, sendAmountTrans.amount, hash2, "qwerty");

    assertTrue(receiveAmountTransOpt1.isPresent());
    assertFalse(receiveAmountTransOpt2.isPresent());
  }

}
