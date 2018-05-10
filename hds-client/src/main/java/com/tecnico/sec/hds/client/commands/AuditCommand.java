package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.PubKey;

import java.util.Optional;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) {

    PubKey key = new PubKey().value(args[0].trim());
    AuditRequest auditRequest = new AuditRequest().publicKey(key);

    Optional<AuditResponse> response = client.server.audit(auditRequest, true);

    if (response.isPresent()) {
      System.out.println(response.get());
    } else {
      System.out.println("Failed to get audit");
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
