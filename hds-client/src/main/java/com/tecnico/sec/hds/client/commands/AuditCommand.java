package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.TransactionInformation;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    AuditRequest auditRequest = new AuditRequest();
    auditRequest.publicKey(client.key);

    AuditResponse auditResponse = client.server.audit(auditRequest);

    for(TransactionInformation transactionInformation : auditResponse.getList()){

    }
  }

  @Override
  public String getName() {
    return name;
  }
}
