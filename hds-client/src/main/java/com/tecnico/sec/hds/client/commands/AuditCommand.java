package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import io.swagger.client.ApiException;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    PubKey key = new PubKey().value(args[0]);
    TransactionGetter transactionGetter = new TransactionGetter();
    AuditRequest auditRequest = new AuditRequest().publicKey(key);
    AuditResponse auditResponse = client.server.audit(auditRequest);

    try {
      StringBuilder transactionListMessage = new StringBuilder();
      for(TransactionInformation transactionInformation : auditResponse.getList()){
        transactionListMessage.append(transactionGetter.getTransactionListMessage(transactionInformation) + "\n");
      }
      if(client.cryptoAgent.verifyBankSignature(transactionListMessage.toString(), auditResponse.getSignature().getValue()))
        System.out.println(transactionListMessage);
      else
        System.out.print("Enexpected error from server. \n Try Again Later.");
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

  }

  @Override
  public String getName() {
    return name;
  }
}
