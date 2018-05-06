package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class SendAmountCommand extends AbstractCommand {
  private static final String name = "send_amount";

  @Override
  public void doRun(Client client, String[] args) {
    PubKey destKey = new PubKey();
    destKey.setValue(args[0].trim());
    Integer amount = Integer.parseInt(args[1].trim());

    AuditRequest auditRequest = new AuditRequest().publicKey(client.server.getKey());

    Optional<AuditResponse> auditResponseOpt = client.server.audit(auditRequest);

    if (auditResponseOpt.isPresent()) {
      Optional<TransactionInformation> transactionOpt =
          Optional.of(auditResponseOpt.get().getList().get(0));


      SendAmountRequest sendAmount = new SendAmountRequest();
      sendAmount.setDestKey(destKey);
      sendAmount.setAmount(amount);

      try {
        System.out.println(client.server.sendAmount(sendAmount, transactionOpt));
      } catch (NoSuchAlgorithmException | IOException | KeyStoreException | InvalidKeySpecException
          | CertificateException | UnrecoverableKeyException | SignatureException | InvalidKeyException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public String getName() {
    return name;
  }
}
