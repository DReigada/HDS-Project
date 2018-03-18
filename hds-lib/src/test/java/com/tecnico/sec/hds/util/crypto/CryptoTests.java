package com.tecnico.sec.hds.util.crypto;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitQuickcheck.class)
public class CryptoTests {
  private static CryptoAgent agent;

  @BeforeClass
  public static void before() throws Exception {
    agent = new CryptoAgent("user1");
  }

  @Property
  public void signatureShouldBeValid(String message) throws Exception {
    String signature = agent.generateSignature(message);
    Boolean valid = agent.verifySignature(message, signature);

    assertTrue(valid);
  }


  @Property
  public void signatureShouldBeInvalid(String message) throws Exception {
    String changedMessage = message + "asfd";

    String signature = agent.generateSignature(message);
    Boolean valid = agent.verifySignature(changedMessage, signature);

    assertFalse(valid);
  }

}
