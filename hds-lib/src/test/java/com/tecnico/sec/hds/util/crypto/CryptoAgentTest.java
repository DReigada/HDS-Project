package com.tecnico.sec.hds.util.crypto;


import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

@RunWith(JUnitQuickcheck.class)
public class CryptoAgentTest {
  private static CryptoAgent agent;
  private static CryptoAgent bank;

  @BeforeClass
  public static void before() throws Exception {
    agent = new CryptoAgent("user1");
    bank = new CryptoAgent("bank");
  }

  @Property
  public void signatureShouldBeValid(String message) throws Exception {
    String signature = bank.generateSignature(message);
    Boolean valid = agent.verifyBankSignature(message, signature);
    assertTrue(valid);
  }

  @Property
  public void signatureShouldBeInvalidIfMessageIsChanged(String message) throws Exception {
    String changedMessage = message + "changed";

    String signature = bank.generateSignature(message);
    Boolean valid = agent.verifyBankSignature(changedMessage, signature);

    assertFalse(valid);
  }

  @Property
  public void signatureShouldBeInvalidIfMessagesAreDifferent(String message1, String message2) throws Exception {
    assumeThat(message1, not(equalTo(message2)));

    String signature = bank.generateSignature(message1);
    Boolean valid = agent.verifyBankSignature(message2, signature);

    if(valid){
      System.out.println(message1);
      System.out.println(message2);
    }
    assertFalse(valid);
  }
}
