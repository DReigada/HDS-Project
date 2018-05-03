package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.AuditRequest;
import io.swagger.client.model.PubKey;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class AuditCommand extends AbstractCommand {
  private static final String name = "audit";

  @Override
  public void doRun(Client client, String[] args) {

    PubKey key = new PubKey().value(args[0]);
    AuditRequest auditRequest = new AuditRequest().publicKey(key);

    try {
      System.out.println(client.server.audit(auditRequest));
    } catch (CertificateException | InvalidKeySpecException | InvalidKeyException | SignatureException
        | KeyStoreException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
