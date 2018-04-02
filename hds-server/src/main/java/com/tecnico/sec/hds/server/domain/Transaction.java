package com.tecnico.sec.hds.server.domain;

public class Transaction {

  private int transID;

  private String sourceKey;

  private String destKey;

  private float amount;

  private boolean pending;

  public Transaction(int transID, String sourceKey, String destKey, float amount, boolean pending){
    this.transID = transID;
    this.sourceKey = sourceKey;
    this.destKey = destKey;
    this.amount = amount;
    this.pending = pending;
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
}
