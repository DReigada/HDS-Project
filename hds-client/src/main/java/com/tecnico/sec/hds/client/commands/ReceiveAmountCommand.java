package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) {
    PubKey sourceKey = new PubKey().value(args[0].trim());
    Hash hash = new Hash().value(args[1].trim());

    AuditRequest auditRequest = new AuditRequest().publicKey(sourceKey);

    Optional<AuditResponse> auditResponseOpt = client.server.audit(auditRequest, true);

    if (auditResponseOpt.isPresent()) {
      Optional<TransactionInformation> transactionOpt =
          auditResponseOpt.get()
              .getList().stream()
              .filter(a -> a.getSendHash().getValue().equals(hash.getValue()))
              .findFirst();

      try {
        if (transactionOpt.isPresent()) {
          TransactionInformation transaction = transactionOpt.get();
          ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

          receiveAmountRequest.setSourceKey(new PubKey().value(transaction.getSourceKey()));
          receiveAmountRequest.setDestKey(new PubKey().value(transaction.getDestKey()));
          receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));
          receiveAmountRequest.setTransHash(transaction.getSendHash());

          Optional<String> receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest);

          System.out.println(receiveAmountResponse.orElse("Failed to call receive amount"));
        }

      } catch (GeneralSecurityException e) {
        System.out.println("Failed to call receive amount");
        e.printStackTrace();
      }
    } else {
      System.out.println("Failed to call Audit in receive amount");
    }

  }

  @Override
  public String getName() {
    return name;
  }
}
