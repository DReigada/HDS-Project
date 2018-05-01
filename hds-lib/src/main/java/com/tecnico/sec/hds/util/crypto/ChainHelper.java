package com.tecnico.sec.hds.util.crypto;

import domain.Transaction;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ChainHelper {
  private static final String SEED_HASH = "0";

  private MessageDigest digest;

  public ChainHelper() {
    try {
      this.digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Could not create transaction hash: this should never happen™ ", e);
    }
  }

  public String generateTransactionHash(Optional<String> previousTransactionHashOpt,
                                        String source,
                                        String dest,
                                        long amount,
                                        TransactionType transType,
                                        String signature) {

    String previousTransactionHash = previousTransactionHashOpt.orElse(SEED_HASH);

    String text = previousTransactionHash + source + dest + amount + transType + signature;

    byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

    return Base64.getEncoder().encodeToString(hash);
  }

  public boolean verifyTransaction(List<Transaction> transactions, String key){
    String newTransactionHash = "";
    for(Transaction transaction : transactions){
      newTransactionHash = generateTransactionHash(Optional.of(newTransactionHash),
          transaction.sourceKey,
          transaction.destKey,
          transaction.amount,
          key.equals(transaction.sourceKey) ? TransactionType.SEND_AMOUNT : TransactionType.ACCEPT,
          transaction.signature);

      System.out.println(newTransactionHash);
      System.out.println(transaction.hash);

      if(!newTransactionHash.equals(transaction.hash)){
        return false;
      }
    }
    return true;
  }



  public enum TransactionType {
    SEND_AMOUNT, ACCEPT
  }

}
