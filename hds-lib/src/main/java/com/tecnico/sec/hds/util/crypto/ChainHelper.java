package com.tecnico.sec.hds.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
                                        float amount,
                                        TransactionType transType,
                                        String signature) {

    String previousTransactionHash = previousTransactionHashOpt.orElse(SEED_HASH);

    String text = previousTransactionHash + source + dest + amount + transType + signature;

    byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

    return Base64.getEncoder().encodeToString(hash);
  }

  public enum TransactionType {
    SEND_AMOUNT, ACCEPT;
  }
}
