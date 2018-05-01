package com.tecnico.sec.hds.util.crypto;

public class ChainHelperTest {
  /*private static List<Transaction> transactionsMixed;
  private static ChainHelper chainHelper;

  @BeforeClass
  public static void before(){
    chainHelper = new ChainHelper();
    transactionsMixed = new ArrayList<>();

    String hash = "";
    for (int i = 0 ; i < 2; i++){
      if(i != 0) {
        hash = chainHelper.generateTransactionHash(
            Optional.of(hash),
            Optional.empty(),
            "User1",
            "User2",
            i-1,
            (i-1 % 2 == 0) ? ChainHelper.TransactionType.SEND_AMOUNT : ChainHelper.TransactionType.ACCEPT,
            "signature");
      }

      transactionsMixed.add(new Transaction(
          i,
          "User1",
          "User2",
          i*2,
          (i % 2 == 0) ? true : false,
          (i % 2 == 0) ? false : true,
          "signature",
          "",
          hash
      ));
    }
  }

  @Test
  public void ChainVerificationWithTransactionsMixed(){
    assertTrue("Not True", chainHelper.verifyTransaction(transactionsMixed, "User1"));
  }*/
}
