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

}
