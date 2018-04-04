package com.tecnico.sec.hds.util.crypto;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoAgent {
    private String username;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public CryptoAgent(String username) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.username = username;
        LoadKeys();
    }

    private void GenerateKey() throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator keygen = KeyPairGenerator .getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG"); // Change After
        keygen.initialize(112,random); // Change After
        KeyPair key = keygen.generateKeyPair();
        publicKey = key.getPublic();
        privateKey = key.getPrivate();
        SaveKeys();
    }

    private void SaveKeys() throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(username + "PublicKey.txt"));
        out.write(getPublicKey());
        out.close();

        out = new BufferedWriter(new FileWriter(username + "PrivateKey.txt"));
        String priKey = convertByteArrToString(privateKey.getEncoded());
        out.write(priKey);
        out.close();
    }

    private void LoadKeys() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        BufferedReader out;
        try {
            publicKey = getPublicKey(username);

            out = new BufferedReader(new FileReader(username + "PrivateKey.txt"));
            String key = out.readLine();
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            out.close();
        } catch (FileNotFoundException e) {
            GenerateKey();
        }
    }

    public String generateSignature(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] data = message.getBytes(); //MESSAGE TO BE SIGNED
        Signature ecForSign = Signature.getInstance("SHA1withECDSA"); //TO BE CHANGED
        ecForSign.initSign(privateKey);
        ecForSign.update(data);
        return convertByteArrToString(ecForSign.sign());
    }

    public String getPublicKey(){
        return convertByteArrToString(publicKey.getEncoded());
    }

    private String convertByteArrToString(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
    }

    public boolean verifyBankSignature(String message, String signature) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        return verifySignature(message, signature, convertByteArrToString(getPublicKey("bank").getEncoded()));
    }

    public boolean verifySignature(String message, String signature, String publicKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException, InvalidKeySpecException {
        PublicKey key = getPublicKey(publicKey);
        byte[] msg = message.getBytes();
        Signature ecForVerify = Signature.getInstance("SHA1withECDSA");
        ecForVerify.initVerify(key);
        byte[] sign = Base64.getDecoder().decode(signature);
        ecForVerify.update(msg);
        return ecForVerify.verify(sign);
    }

    private PublicKey getPublicKey(String username) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        BufferedReader out = new BufferedReader(new FileReader(username + "PublicKey.txt"));
        String key = out.readLine();
        KeyFactory  keyFactory = KeyFactory.getInstance("EC");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        out.close();
        return publicKey;
    }
}