package com.tecnico.sec.hds.util.crypto;


import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

@RunWith(JUnitQuickcheck.class)
public class CryptoAgentTest {
  private static CryptoAgent agent;
  private static CryptoAgent bank;

  private static String serverUrl = "http://test:8080";

  @BeforeClass
  public static void before() throws Exception {
    agent = new CryptoAgent("user1", "pass");
    bank = new CryptoAgent("banktest_8080", "banktest_8080");
  }

  @Property
  public void signatureShouldBeValid(String message) throws Exception {
    String signature = bank.generateSignature(message);
    Boolean valid = agent.verifyBankSignature(message, signature, serverUrl);
    assertTrue(valid);
  }

  @Property
  public void signatureShouldBeInvalidIfMessageIsChanged(String message) throws Exception {
    String changedMessage = message + "changed";

    String signature = bank.generateSignature(message);
    Boolean valid = agent.verifyBankSignature(changedMessage, signature, serverUrl);

    assertFalse(valid);
  }

  @Property
  public void signatureShouldBeInvalidIfMessagesAreDifferent(String message1, String message2) throws Exception {
    assumeThat(message1, not(equalTo(message2)));

    String signature = bank.generateSignature(message1);
    Boolean valid = agent.verifyBankSignature(message2, signature, serverUrl);

    if (valid) {
      System.out.println(message1);
      System.out.println(message2);
    }
    assertFalse(valid);
  }

  @Test
  public void certificateNotNull() throws Exception {
    X509Certificate x509Certificate = agent.generateSelfSignX509Certificate("user1");
    assertNotNull(x509Certificate);
  }

  @Test(expected = SignatureException.class)
  public void certificateVerifyWorngKey() throws Exception {
    X509Certificate x509Certificate = agent.generateSelfSignX509Certificate("user1");

    KeyPairGenerator keygen = KeyPairGenerator.getInstance("EC");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
    keygen.initialize(112, random); // Change After
    KeyPair key = keygen.generateKeyPair();
    PublicKey publicKey = key.getPublic();

    x509Certificate.verify(publicKey);
  }

  @Test
  public void certificateVerifyRightKey() throws Exception {
    X509Certificate x509Certificate = agent.generateSelfSignX509Certificate("user1");

    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    byte[] keyBytes = Base64.getDecoder().decode(agent.getStringPublicKey());
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);

    x509Certificate.verify(publicKey);
  }

  @Test
  public void certificatePubKey() throws Exception {
    X509Certificate x509Certificate = agent.generateSelfSignX509Certificate("user1");
    assertEquals(agent.getStringPublicKey(), Base64.getEncoder().encodeToString(x509Certificate.getPublicKey().getEncoded()));
  }

  @Test
  public void loadKeysSucess() throws Exception {
    KeyStore ks = agent.getKeyStore("user1");
    PublicKey agentKey = ks.getCertificate("user1pub").getPublicKey();
    String key = Base64.getEncoder().encodeToString(agentKey.getEncoded());
    assertEquals(agent.getStringPublicKey(), key);
  }

}
