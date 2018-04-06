package com.tecnico.sec.hds.util.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class CryptoAgent {
  private String username;
  private PublicKey publicKey;
  private PrivateKey privateKey;
  private SecretKey secretKey;
  private byte[] initVector; //FIXME:really use a random string

  public CryptoAgent(String username, String password) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException  {
    this.username = username;

    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), username.getBytes(), 65536, 128);
    SecretKey temporaryKey = factory.generateSecret(spec);
    this.secretKey = new SecretKeySpec(temporaryKey.getEncoded(), "AES");

    LoadKeys();
  }

  private void GenerateKey() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, IOException,
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    KeyPairGenerator keygen = KeyPairGenerator.getInstance("EC");
    SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
    keygen.initialize(112, random); // Change After
    KeyPair key = keygen.generateKeyPair();
    publicKey = key.getPublic();
    privateKey = key.getPrivate();
    SaveKeys();
  }

  private void SaveKeys() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, IOException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {
    BufferedWriter out = new BufferedWriter(new FileWriter(username + "PublicKey.txt"));
    out.write(getStringPublicKey());
    out.close();

    byte[] bytes = new byte[16];
    SecureRandom.getInstance("SHA1PRNG").nextBytes(bytes);
    String iv = convertByteArrToString(bytes);
    initVector = convertStringToByteArr(iv);
    out = new BufferedWriter(new FileWriter(username + "IV.txt"));
    out.write(iv);
    out.close();

    out = new BufferedWriter(new FileWriter(username + "PrivateKey.txt"));
    String priKey = convertByteArrToString(privateKey.getEncoded());
    String encriptedKey = cipherPrivateKey(priKey);
    out.write(encriptedKey);
    out.close();

  }

  private void LoadKeys() throws InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException  {
    BufferedReader in;
    try {
      publicKey = getPublicKeyFromFile(username);

      in = new BufferedReader(new FileReader(username + "IV.txt"));
      String iv = in.readLine();
      initVector = convertStringToByteArr(iv);

      in = new BufferedReader(new FileReader(username + "PrivateKey.txt"));
      String encriptedkey = in.readLine();
      String key = decipherPrivateKey(encriptedkey);
      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      byte[] keyBytes = Base64.getDecoder().decode(key);
      PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
      privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
      in.close();


    } catch (FileNotFoundException e) {
      GenerateKey();
    }
  }

  private String cipherPrivateKey(String privateKey) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException,
    IllegalBlockSizeException, NoSuchPaddingException, BadPaddingException {

    IvParameterSpec iv = new IvParameterSpec(initVector);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

    byte[] key = cipher.doFinal(convertStringToByteArr(privateKey));

    return convertByteArrToString(key);
  }

  private String decipherPrivateKey(String encryptedKey) throws InvalidParameterSpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

    IvParameterSpec iv = new IvParameterSpec(initVector);

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);


    byte[] original = cipher.doFinal(convertStringToByteArr(encryptedKey));

    return convertByteArrToString(original);
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

  private byte[] convertStringToByteArr(String bytes) {
    return Base64.getDecoder().decode(bytes);
  }

  public boolean verifyBankSignature(String message, String signature) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    PublicKey bankPubKey = getPublicKeyFromFile("bank");
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

  private PublicKey getPublicKeyFromFile(String username) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    BufferedReader out = new BufferedReader(new FileReader(username + "PublicKey.txt"));
    String key = out.readLine();
    out.close();
    return getPublicKeyFromString(key);
  }

  private PublicKey getPublicKeyFromString(String key) throws InvalidKeySpecException, NoSuchAlgorithmException {
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    byte[] keyBytes = Base64.getDecoder().decode(key);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }
}