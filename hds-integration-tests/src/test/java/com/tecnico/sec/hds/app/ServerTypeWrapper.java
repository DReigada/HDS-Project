package com.tecnico.sec.hds.app;

public class ServerTypeWrapper {

  private ServerType type;

  public ServerTypeWrapper(String type){
    this.type = ServerType.valueOf(type);
  }

  public ServerType getType() {
    return type;
  }

  public enum ServerType {
    BYZANTINE, NORMAL, BADSIGN
  }
}
