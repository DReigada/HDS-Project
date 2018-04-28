package com.tecnico.sec.hds.util.crypto;

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

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoAgent {
  private String username;
  private PublicKey publicKey;
  private PrivateKey privateKey;

  public CryptoAgent(String username, String password) throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, OperatorCreationException, KeyStoreException {
    this.username = username;
    Security.addProvider(new BouncyCastleProvider());
    LoadKeys(password);
  }

  private void GenerateKey() throws NoSuchAlgorithmException {
    KeyPairGenerator keygen = KeyPairGenerator.getInstance("EC");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
    keygen.initialize(112, random); // Change After
    KeyPair key = keygen.generateKeyPair();
    publicKey = key.getPublic();
    privateKey = key.getPrivate();
  }

  private void LoadKeys(String passWord) throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, OperatorCreationException {
    try {
      KeyStore ks = getKeyStore(username, passWord);

      privateKey = (PrivateKey) ks.getKey(username + "priv", passWord.toCharArray());
      publicKey = (PublicKey) ks.getKey(username + "pub", passWord.toCharArray());
    } catch (FileNotFoundException e) {
      GenerateKey();
      createKeyStore(passWord);
    }
  }

  public KeyStore getKeyStore(String username, String passWord) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    FileInputStream fis = new FileInputStream(username + "KeyStore.jce");
    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(fis, passWord.toCharArray());
    fis.close();
    return ks;
  }


  public String generateSignature(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    byte[] data = message.getBytes(); //MESSAGE TO BE SIGNED
    Signature ecForSign = Signature.getInstance("SHA1withECDSA"); //TO BE CHANGED
    ecForSign.initSign(privateKey);
    ecForSign.update(data);
    return convertByteArrToString(ecForSign.sign());
  }

  public String getStringPublicKey() {
    return convertByteArrToString(publicKey.getEncoded());
  }

  private String convertByteArrToString(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  public boolean verifyBankSignature(String message, String signature) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException, KeyStoreException, UnrecoverableKeyException {
    KeyStore ks = getKeyStore("bank", "bank");
    PublicKey bankPubKey = (PublicKey) ks.getKey("bankpub", "bank".toCharArray());
    String key = convertByteArrToString(bankPubKey.getEncoded());
    return verifySignature(message, signature, key);
  }

  public boolean verifySignature(String message, String signature, String publicKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
    PublicKey key = getPublicKeyFromString(publicKey);
    byte[] msg = message.getBytes();
    Signature ecForVerify = Signature.getInstance("SHA1withECDSA");
    ecForVerify.initVerify(key);
    byte[] sign = Base64.getDecoder().decode(signature);
    ecForVerify.update(msg);
    return ecForVerify.verify(sign);
  }

  private PublicKey getPublicKeyFromString(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    byte[] keyBytes = Base64.getDecoder().decode(key);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }

  public X509Certificate generateSelfSignX509Certificate(String username) throws NoSuchAlgorithmException, OperatorCreationException, CertificateException {
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

    return new JcaX509CertificateConverter().setProvider( "BC" ).getCertificate(certificateHolder );
  }

  public void createKeyStore(String passWord) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, OperatorCreationException {
    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(null, passWord.toCharArray());

    X509Certificate certificate =generateSelfSignX509Certificate(username);
    Certificate certificateChain[] = new Certificate[1];
    certificateChain[0] = certificate;

    ks.setKeyEntry(username + "priv", privateKey, passWord.toCharArray(), certificateChain);
    ks.setKeyEntry(username + "pub", publicKey, passWord.toCharArray(), certificateChain);

    saveKeyStore(ks,passWord);
  }

  public void saveKeyStore(KeyStore keyStore, String passWord) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
    FileOutputStream fos = new FileOutputStream(username + "KeyStore.jce");
    keyStore.store(fos, passWord.toCharArray());
    fos.close();
  }
}