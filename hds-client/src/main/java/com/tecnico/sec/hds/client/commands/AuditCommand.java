package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import domain.Transaction;
import io.swagger.client.ApiException;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.AuditResponse;
import io.swagger.client.model.PubKey;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.List;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    PubKey key = new PubKey().value(args[0]);
    AuditRequest auditRequest = new AuditRequest().publicKey(key);
    AuditResponse auditResponse = client.server.audit(auditRequest);

    try {
      if (auditResponse.getList() != null) {
        String transactionListMessage = TransactionGetter.getTransactionListMessage(auditResponse.getList());
        List<Transaction> transactions = TransactionGetter.InformationToTransaction(auditResponse.getList());
        Collections.reverse(transactions);

        if (client.cryptoAgent.verifyBankSignature(transactionListMessage, auditResponse.getSignature().getValue())
            && client.cryptoAgent.verifyTransactionsSignature(transactions)
            && client.chainHelper.verifyTransaction(transactions, key.getValue())) {

          System.out.println(transactionListMessage);
          if (key.equals(client.key)) {
            client.setLastHash(auditResponse.getList().get(0).getSendHash());
          }
        } else {
          System.out.print("Unexpected error from server. \n Try Again Later.");
        }
      }
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
      e.printStackTrace();
    }

  }

  @Override
  public String getName() {
    return name;
  }
}
