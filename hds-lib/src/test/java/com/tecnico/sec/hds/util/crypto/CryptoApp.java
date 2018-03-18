package com.tecnico.sec.hds.util.crypto;

public class CryptoApp {


  public static void main(String[] args) throws Exception {
    String message = "asdf";

    CryptoAgent agent = new CryptoAgent("user1");

    String signature = agent.generateSignature(message);
    Boolean valid = agent.verifySignature("f", signature);

    System.out.println(valid);
  }

}
