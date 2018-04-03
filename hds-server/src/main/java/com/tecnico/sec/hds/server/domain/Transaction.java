package com.tecnico.sec.hds.server.domain;

public class Transaction {

  public final int transID;

  public final String sourceKey;

  public final String destKey;

  public final float amount;

  public final boolean pending;

  public final String signature;

  public final String hash;

  public Transaction(int transID, String sourceKey, String destKey, float amount, boolean pending, String signature, String hash) {
    this.transID = transID;
    this.sourceKey = sourceKey;
    this.destKey = destKey;
    this.amount = amount;
    this.pending = pending;
    this.signature = signature;
    this.hash = hash;
  }

  public int getTransID() {
    return transID;
  }

  public String getSourceKey() {
    return sourceKey;
  }

  public String getDestKey() {
    return destKey;
  }

  public float getAmount() {
    return amount;
  }

  public boolean isPending() {
    return pending;
  }

  public String getHash() {
    return hash;
  }

  public String getSignature() {
    return signature;
  }
}
