package com.tecnico.sec.hds.server.controllers.util;

import java.util.HashSet;
import java.util.Set;

public class ReliableBroadcastSession {

  // Strings are the servers' public keys
  private final Set<String> echos;
  private final Set<String> readys;
  private final int servers;
  private boolean sentEcho;
  private boolean sentReady;
  private boolean delivered;

  private int numberOfFailures;

  public ReliableBroadcastSession(int servers) {
    sentEcho = false;
    sentReady = false;
    delivered = false;
    echos = new HashSet<>();
    readys = new HashSet<>();

    this.servers = servers;
    this.numberOfFailures = (int) ((servers - 1) / 3.0);
  }

  public synchronized void monitorWait() {
    try {
      this.wait();
    } catch (Throwable e) {
      throw new RuntimeException("Failed to wait on session", e);
    }
  }


  public synchronized void monitorNotify() {
    try {
      this.notifyAll();
    } catch (Throwable e) {
      throw new RuntimeException("Failed to notify on session", e);
    }
  }

  public synchronized void putEcho(String serverPublicKey) {
    echos.add(serverPublicKey);
  }

  public synchronized void putReady(String serverPublicKey) {
    readys.add(serverPublicKey);
  }

  public synchronized boolean canBroadcastEcho() {
    if (!sentEcho) {
      sentEcho = true;
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean canBroadcastReadyAfterEcho() {
    if (!sentReady && getEchosSize() > echoMajority()) {
      sentReady = true;
      return true;
    } else {
      return false;
    }
  }


  public synchronized boolean canBroadcastReadyAfterReady() {
    if (!sentReady && getReadysSize() > readyMajorityToSend()) {
      sentReady = true;
      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean canDeliver() {
    if (!delivered && getReadysSize() > readyMajorityToDeliver()) {
      delivered = true;
      return true;
    } else {
      return false;
    }
  }

  private int echoMajority() {
    return (int) ((servers + numberOfFailures) / 2.0);
  }

  private int readyMajorityToSend() {
    return numberOfFailures;
  }

  private int readyMajorityToDeliver() {
    return 2 * numberOfFailures;
  }

  private int getEchosSize() {
    return echos.size();
  }

  private int getReadysSize() {
    return readys.size();
  }

}
