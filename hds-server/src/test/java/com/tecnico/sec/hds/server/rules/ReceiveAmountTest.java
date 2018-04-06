package com.tecnico.sec.hds.server.rules;

public class ReceiveAmountTest {
  /*@BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }


  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(createRandomAccount(), createRandomAccount(), 1, "a", "");

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), sendAmountTrans.sourceKey, sendAmountTrans.destKey,
        sendAmountTrans.amount, ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.empty(), receiveAmountTrans.sourceKey, receiveAmountTrans.destKey,
        receiveAmountTrans.amount, ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.signature);

    assertEquals(hash1, sendAmountTrans.hash);
    assertEquals(hash2, receiveAmountTrans.hash);
  }


  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    String destAcc = createRandomAccount();

    Optional<Transaction> previousTransOpt = sendAmountRules.sendAmount(destAcc, createRandomAccount(), 1, "a", "");
    assertTrue(previousTransOpt.isPresent());

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(createRandomAccount(), destAcc, 1, "a", "");

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");

    assertTrue(receiveAmountTransOpt.isPresent());
    Transaction receiveAmountTrans = receiveAmountTransOpt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), sendAmountTrans.sourceKey, sendAmountTrans.destKey,
        sendAmountTrans.amount, ChainHelper.TransactionType.SEND_AMOUNT, sendAmountTrans.signature);

    String hash2 = new ChainHelper().generateTransactionHash(previousTransOpt.map(t -> t.hash),
        receiveAmountTrans.sourceKey, receiveAmountTrans.destKey,
        receiveAmountTrans.amount, ChainHelper.TransactionType.ACCEPT, receiveAmountTrans.signature);

    assertEquals(hash1, sendAmountTrans.hash);
    assertEquals(hash2, receiveAmountTrans.hash);
  }

  @Test
  public void shouldNotAcceptTheSameTransactionMoreThanOnce() throws DBException {
    SendAmountRules sendAmountRules = new SendAmountRules();
    ReceiveAmountRules receiveAmountRules = new ReceiveAmountRules();

    String destAcc = createRandomAccount();

    Optional<Transaction> sendAmountTransOpt = sendAmountRules.sendAmount(createRandomAccount(), destAcc, 1, "a", "");

    assertTrue(sendAmountTransOpt.isPresent());
    Transaction sendAmountTrans = sendAmountTransOpt.get();

    Optional<Transaction> receiveAmountTransOpt1 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");
    Optional<Transaction> receiveAmountTransOpt2 = receiveAmountRules.receiveAmount(sendAmountTrans.hash, "b");

    assertTrue(receiveAmountTransOpt1.isPresent());
    assertFalse(receiveAmountTransOpt2.isPresent());
  }*/

}
