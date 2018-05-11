package com.tecnico.sec.hds.app;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ServerTypeWrapper {

  private static List<ServerTypeWrapper> wrappers = new LinkedList<>();

  private ServerType type;

  public ServerTypeWrapper(String type){
    this.type = ServerType.valueOf(type);
  }

  public synchronized static void add(ServerTypeWrapper wrapper) {
    wrappers.add(wrapper);
  }

  public synchronized static ArrayList<ServerTypeWrapper> get() {
    return new ArrayList<>(wrappers);
  }

  public ServerType getType() {
    return type;
  }

  public void setType(ServerType type) {
    this.type = type;
  }

  public enum ServerType {
    BYZANTINE, NORMAL, BADSIGN
  }
}
