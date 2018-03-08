package domain;

public class Transfer {

  private int transID;

  private String sourceX;

  private String sourceY;

  private String destX;

  private String destY;

  private float amount;

  private boolean pending;

  public Transfer(int transID, String sourceX, String sourceY, String destX, String destY, float amount, boolean pending){
    this.transID = transID;
    this.sourceX = sourceX;
    this.sourceY = sourceY;
    this.destX = destX;
    this.destY = destY;
    this.amount = amount;
    this.pending = pending;
  }

  public int getTransID() {
    return transID;
  }

  public String getSourceX() {
    return sourceX;
  }

  public String getSourceY() {
    return sourceY;
  }

  public String getDestX() {
    return destX;
  }

  public String getDestY() {
    return destY;
  }

  public float getAmount() {
    return amount;
  }

  public boolean isPending() {
    return pending;
  }
}
