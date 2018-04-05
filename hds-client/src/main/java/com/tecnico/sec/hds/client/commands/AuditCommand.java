package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.PubKey;
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
    PubKey key = new PubKey().value(args[0]);
    AuditRequest auditRequest = new AuditRequest().publicKey(key);
    AuditResponse auditResponse = client.server.audit(auditRequest);

    try {
      StringBuilder transactionListMessage = new StringBuilder();
      for(TransactionInformation transactionInformation : auditResponse.getList()){
        transactionListMessage.append(getTransactionListMessage(transactionInformation) + "\n");
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

  private String getTransactionListMessage(TransactionInformation transaction){
    String transactionListMessage = "";
    transactionListMessage += "Transaction ID: " + transaction.getTransID() + "\n";
    transactionListMessage += "Source Key: " + transaction.getSourceKey() + "\n";
    transactionListMessage += "Destination Key: " + transaction.getDestKey() + "\n";
    transactionListMessage += "Amount: " + transaction.getAmount() + "\n";
    transactionListMessage += "Pending: " + transaction.isPending() + "\n";
    transactionListMessage += "Received: " + transaction.isReceive() + "\n";
    transactionListMessage += "Signature: " + transaction.getSignature().getValue() + "\n";
    transactionListMessage += "Hash: " + transaction.getHash().getValue() + "\n";
    return transactionListMessage;
  }
}
