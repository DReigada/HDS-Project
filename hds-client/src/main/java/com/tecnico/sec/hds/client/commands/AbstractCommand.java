package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;

abstract public class AbstractCommand {
  public final void run(Client client, String[] args) {
    try {
      doRun(client, args);
    } catch (Exception e) {
      System.err.println("Failed to run command. Reason: " + e.getMessage());
      e.printStackTrace();
    }
  }


  protected abstract void doRun(Client client, String[] args) throws Exception;

  public abstract String getName();
}
