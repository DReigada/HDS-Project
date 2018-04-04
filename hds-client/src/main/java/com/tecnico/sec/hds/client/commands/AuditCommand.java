package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.TransactionInformation;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    AuditRequest auditRequest = new AuditRequest();
    auditRequest.publicKey(client.key);

    AuditResponse auditResponse = client.server.audit(auditRequest);
    List<TransactionInformation> history = auditResponse.getList();

    try {
      client.cryptoAgent.verifyBankSignature(history.toString() , auditResponse.getSignature().getValue());
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

    for(TransactionInformation transactionInformation : auditResponse.getList()){

    }
  }

  @Override
  public String getName() {
    return name;
  }
}
