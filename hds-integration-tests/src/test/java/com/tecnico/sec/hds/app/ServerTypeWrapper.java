package com.tecnico.sec.hds.app;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class ServerTypeWrapper {

  private static List<ServerTypeWrapper> wrappers = new LinkedList<>();

  private ServerType type;

  public ServerTypeWrapper(String type) {
    this.type = ServerType.valueOf(type);
  }

  public synchronized static void add(ServerTypeWrapper wrapper) {
    wrappers.add(wrapper);
  }

//  public synchronized static ArrayList<ServerTypeWrapper> get() {
//    return new ArrayList<>(wrappers);
//  }

  public synchronized static void changeServerType(int i, ServerType type) {
    wrappers.get(i).setType(type);
  }

  public synchronized static void changeServersType(int first, int last,
                                                    ServerTypeWrapper.ServerType type) {
    IntStream.range(first, last).forEach(i -> changeServerType(i, type));
  }

  public synchronized static void cleanServers() {
    wrappers = new LinkedList<>();
  }

  public ServerType getType() {
    return type;
  }

  public void setType(ServerType type) {
    this.type = type;
  }

  public enum ServerType {
    BYZANTINE, NORMAL, BADSIGN, BADORDER, SAMEBADORDER, NOECHOES, NOREADIES, ECHOS10, READIES2
  }
}
