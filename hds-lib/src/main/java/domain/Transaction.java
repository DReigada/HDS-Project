package domain;

public class Transaction {

  public final int transID;

  public final String sourceKey;

  public final String destKey;

  public final long amount;

  public final boolean pending;

  public final boolean receive;

  public final String hash;

  public final String receiveHash;

  public final String signature;

  public Transaction(int transID, String sourceKey, String destKey, long amount, boolean pending, boolean receive, String hash, String receiveHash, String signature) {
    this.transID = transID;
    this.sourceKey = sourceKey;
    this.destKey = destKey;
    this.amount = amount;
    this.pending = pending;
    this.receive = receive;
    this.hash = hash;
    this.receiveHash = receiveHash;
    this.signature = signature;
  }

}
