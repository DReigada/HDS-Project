package com.tecnico.sec.hds.server.controllers.util;

import java.util.HashMap;
import java.util.Map;

public class ReliableBroadcastHelper {
  // the keys are the hashes
  private final Map<String, ReliableBroadcastSession> reliableBroadcastSessions;

  public ReliableBroadcastHelper() {
    reliableBroadcastSessions = new HashMap<>();
  }

  /**
   * @param hash
   * @return true if the element existed, false otherwise
   */
  public synchronized ReliableBroadcastSession createIfNotExists(String hash) {
    return reliableBroadcastSessions.computeIfAbsent(hash, s -> new ReliableBroadcastSession());
  }

  public synchronized ReliableBroadcastSession get(String hash) {
    return reliableBroadcastSessions.get(hash);
  }

}
