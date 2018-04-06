package com.tecnico.sec.hds.server.rules;

public class SendAmountTest {

 /* @BeforeClass
  public static void beforeClass() {
    Migrations.migrate();
  }

/*
  @Test
  public void hashesShouldBeValidForIndependentTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    Optional<Transaction> t1opt = rules.sendAmount(createRandomAccount(), createRandomAccount(), 1, "a", "");
    Optional<Transaction> t2opt = rules.sendAmount(createRandomAccount(), createRandomAccount(), 2, "b", "");

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), t1.sourceKey, t1.destKey,
        t1.amount, ChainHelper.TransactionType.SEND_AMOUNT, t1.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.empty(), t2.sourceKey, t2.destKey,
        t2.amount, ChainHelper.TransactionType.SEND_AMOUNT, t2.signature);

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
  }

  @Test
  public void hashesShouldBeValidForSequentialTransactions() throws Exception {
    SendAmountRules rules = new SendAmountRules();

    String account = createRandomAccount();

    Optional<Transaction> t1opt = rules.sendAmount(account, createRandomAccount(), 1, "a", "");
    Optional<Transaction> t2opt = rules.sendAmount(account, createRandomAccount(), 2, "b", "");

    assertTrue(t1opt.isPresent());
    assertTrue(t2opt.isPresent());

    Transaction t1 = t1opt.get();
    Transaction t2 = t2opt.get();

    String hash1 = new ChainHelper().generateTransactionHash(Optional.empty(), t1.sourceKey, t1.destKey,
        t1.amount, ChainHelper.TransactionType.SEND_AMOUNT, t1.signature);

    String hash2 = new ChainHelper().generateTransactionHash(Optional.of(t1.hash), t2.sourceKey, t2.destKey,
        t2.amount, ChainHelper.TransactionType.SEND_AMOUNT, t2.signature);

    assertEquals(hash1, t1.hash);
    assertEquals(hash2, t2.hash);
<<<<<<< HEAD
  }
*/
}
