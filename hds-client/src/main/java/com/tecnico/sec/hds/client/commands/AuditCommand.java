package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.PubKey;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) {

    PubKey key = new PubKey().value(args[0]);
    AuditRequest auditRequest = new AuditRequest().publicKey(key);

    System.out.println(client.server.audit(auditRequest));
  }

  @Override
  public String getName() {
    return name;
  }
}
