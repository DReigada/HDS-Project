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

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    AuditRequest auditRequest = new AuditRequest();
    auditRequest.setPublicKey(client.key);

    AuditResponse auditResponse = client.server.audit(auditRequest);

    try {
      StringBuilder transactionListMessage = new StringBuilder();
      for(TransactionInformation transactionInformation : auditResponse.getList()){
        transactionListMessage.append(getTransactionListMessage(transactionInformation));
      }

      client.cryptoAgent.verifyBankSignature(transactionListMessage.toString(), auditResponse.getSignature().getValue());
      System.out.println(transactionListMessage);
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

  }

  @Override
  public String getName() {
    return name;
  }

  private String getTransactionListMessage(TransactionInformation transaction){
    String transactionListMessage = "";
    transactionListMessage += transaction.getTransID() + "\n";
    transactionListMessage += transaction.getSourceKey() + "\n";
    transactionListMessage += transaction.getDestKey() + "\n";
    transactionListMessage += transaction.getAmount() + "\n";
    transactionListMessage += transaction.isPending() + "\n";
    transactionListMessage += transaction.isReceive() + "\n";
    transactionListMessage += transaction.getSignature() + "\n";
    transactionListMessage += transaction.getHash() + "\n";
    return transactionListMessage;
  }
}
