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
                                        Optional<String> receiveHash,
                                        String source,
                                        String dest,
                                        long amount,
                                        TransactionType transType) {

    String previousTransactionHash = previousTransactionHashOpt.orElse(SEED_HASH);

    String text = previousTransactionHash + receiveHash.orElse("") + source + dest + amount + transType;

    byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

    return Base64.getEncoder().encodeToString(hash);
  }

  public boolean verifyTransaction(List<Transaction> transactions){
    String newTransactionHash = "";
    for(Transaction transaction : transactions){
      newTransactionHash = generateTransactionHash(Optional.of(newTransactionHash),
          Optional.of(transaction.receiveHash),
          transaction.sourceKey,
          transaction.destKey,
          transaction.amount,
          !transaction.receive ? TransactionType.SEND_AMOUNT : TransactionType.ACCEPT);

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
