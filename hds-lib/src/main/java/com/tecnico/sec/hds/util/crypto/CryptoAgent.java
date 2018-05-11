package com.tecnico.sec.hds.util.crypto;

import com.tecnico.sec.hds.util.crypto.exceptions.CryptoAgentException;
import domain.Transaction;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.joda.time.DateTime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class CryptoAgent {
  private final char[] GLOBAL_KS_PASS;
  public String username;
  private PublicKey publicKey;
  private PrivateKey privateKey;

  public CryptoAgent(String username, String password) {
    this.username = username;
    GLOBAL_KS_PASS = "ks".toCharArray();
    Security.addProvider(new BouncyCastleProvider());
    LoadKeys(password);
  }

  private void GenerateKey() {
    try {
      KeyPairGenerator keygen = KeyPairGenerator.getInstance("EC");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
      keygen.initialize(112, random); // Change After
      KeyPair key = keygen.generateKeyPair();
      publicKey = key.getPublic();
      privateKey = key.getPrivate();
    } catch (GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed generating Key", e);
    }
  }

  private void LoadKeys(String passWord) {
    try {
      try {
        KeyStore ks = getKeyStore(username);

        privateKey = (PrivateKey) ks.getKey(username + "priv", passWord.toCharArray());
        publicKey = ks.getCertificate(username + "pub").getPublicKey();
      } catch (FileNotFoundException e) {
        GenerateKey();
        createKeyStore(passWord);
      }
    } catch (IOException | GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed loading Key", e);
    }
  }

  KeyStore getKeyStore(String username) throws IOException {
    try {
      FileInputStream fis = new FileInputStream(username + "KeyStore.jce");
      KeyStore ks = KeyStore.getInstance("JCEKS");
      ks.load(fis, GLOBAL_KS_PASS);
      fis.close();
      return ks;
    } catch (GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed getting Key store", e);
    }
  }

  private Certificate getBankCertificate(String url) {
    try {
      URL javaUrl = new URL(url);

      boolean useLocalhost = Boolean.valueOf(System.getProperty("hds.coin.crypto.useLocalhost", "true"));
      String hostName = useLocalhost ? "localhost" : javaUrl.getHost().replace(".", "_");

      String name = "bank" + hostName + "_" + javaUrl.getPort();

      KeyStore ks = getKeyStore(name);
      return ks.getCertificate(name + "pub");
    } catch (IOException | GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed getting bank certificate", e);
    }
  }

  public String getBankPublicKey(String url) {
    return convertByteArrToString(getBankCertificate(url).getPublicKey().getEncoded());
  }

  public String generateSignature(String message) {
    try {
      byte[] data = message.getBytes(); //MESSAGE TO BE SIGNED
      Signature ecForSign = Signature.getInstance("SHA1withECDSA"); //TO BE CHANGED
      ecForSign.initSign(privateKey);
      ecForSign.update(data);
      return convertByteArrToString(ecForSign.sign());
    } catch (GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed generating signature", e);
    }
  }

  public String getStringPublicKey() {
    return convertByteArrToString(publicKey.getEncoded());
  }

  private String convertByteArrToString(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  public boolean verifyBankSignature(String message, String signature, String url) {
    PublicKey bankPubKey = getBankCertificate(url).getPublicKey();
    String key = convertByteArrToString(bankPubKey.getEncoded());
    return verifySignature(message, signature, key);
  }

  public boolean verifySignature(String message, String signature, String publicKey) {
    try {
      PublicKey key = getPublicKeyFromString(publicKey);
      byte[] msg = message.getBytes();
      Signature ecForVerify = Signature.getInstance("SHA1withECDSA");
      ecForVerify.initVerify(key);
      byte[] sign = Base64.getDecoder().decode(signature);
      ecForVerify.update(msg);
      return ecForVerify.verify(sign);
    } catch (GeneralSecurityException e) {
      System.err.println("CryptoAgent: Failed verifying signature");
      e.printStackTrace();
      return false;
    }
  }

  private PublicKey getPublicKeyFromString(String key) {
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      byte[] keyBytes = Base64.getDecoder().decode(key);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      PublicKey publicKey = keyFactory.generatePublic(keySpec);
      return publicKey;
    } catch (GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed verifying signature", e);
    }
  }

  public X509Certificate generateSelfSignX509Certificate(String username) {
    try {
      DateTime validityBeginDate = new DateTime();
      DateTime validityEndDate = new DateTime().plusYears(2);

      X500Name dnName = new X500Name("CN=HDS");
      X500Name subject = new X500Name("CN=" + username);
      BigInteger serialNumber = new BigInteger(30, SecureRandom.getInstanceStrong());

    /*AlgorithmIdentifier algorithmIdentifier = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withECDSA");
    SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algorithmIdentifier, ASN1Sequence.getInstance(publicKey.getEncoded()));*/

      SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(publicKey.getEncoded()));

      X509v1CertificateBuilder certificate = new X509v1CertificateBuilder(
          dnName,
          serialNumber,
          validityBeginDate.toDate(), validityEndDate.toDate(),
          subject,
          subjectPublicKeyInfo);

      X509CertificateHolder certificateHolder = certificate
          .build(new JcaContentSignerBuilder("SHA1withECDSA").build(privateKey));

      return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
    } catch (GeneralSecurityException | OperatorCreationException e) {
      throw new CryptoAgentException("CryptoAgent: Failed generating certificate", e);
    }
  }

  public void createKeyStore(String passWord) {
    try {
      KeyStore ks = KeyStore.getInstance("JCEKS");
      ks.load(null, GLOBAL_KS_PASS);

      X509Certificate certificate = generateSelfSignX509Certificate(username);
      Certificate certificateChain[] = new Certificate[1];
      certificateChain[0] = certificate;

      ks.setKeyEntry(username + "priv", privateKey, passWord.toCharArray(), certificateChain);
      ks.setCertificateEntry(username + "pub", certificate);

      saveKeyStore(ks);
    } catch (GeneralSecurityException | IOException e) {
      throw new CryptoAgentException("CryptoAgent: Failed creating key store", e);
    }
  }

  public void saveKeyStore(KeyStore keyStore) {
    try {
      FileOutputStream fos = new FileOutputStream(username + "KeyStore.jce");
      keyStore.store(fos, GLOBAL_KS_PASS);
      fos.close();
    } catch (IOException | GeneralSecurityException e) {
      throw new CryptoAgentException("CryptoAgent: Failed saving key store", e);
    }
  }

  public boolean verifyTransactionsSignature(List<Transaction> transactions, String publicKey) {
    for (int i = 1; i < transactions.size(); i++) {
      String message = transactions.get(i).sourceKey + transactions.get(i).destKey + transactions.get(i).amount
          + transactions.get(i).hash + transactions.get(i).receiveHash;

      if (transactions.get(i).signature.isEmpty() ||
          !verifySignature(message, transactions.get(i).signature, publicKey)) {
        return false;
      }
    }
    return true;
  }

}