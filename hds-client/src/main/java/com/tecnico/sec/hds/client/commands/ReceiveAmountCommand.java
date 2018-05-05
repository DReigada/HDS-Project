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
    PubKey sourceKey = new PubKey().value(args[0]);
    Hash hash = new Hash().value(args[1]);

    AuditRequest auditRequest = new AuditRequest().publicKey(sourceKey);

    Optional<AuditResponse> auditResponseOpt = client.server.audit(auditRequest);

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
          receiveAmountRequest.setTransHash(transaction.getReceiveHash());

          boolean receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest);

          if (receiveAmountResponse) {
            System.out.println("Receive amount successful");
          } else {
            System.out.println("Failed to call receive amount");
          }
        }

      } catch (CertificateException | InvalidKeySpecException | InvalidKeyException | SignatureException
          | KeyStoreException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
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
