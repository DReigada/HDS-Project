package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.AuditRequest;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    AuditRequest auditRequest = new AuditRequest();
    auditRequest.publicKey(client.key);
    client.server.audit(auditRequest);
  }

  @Override
  public String getName() {
    return name;
  }
}
