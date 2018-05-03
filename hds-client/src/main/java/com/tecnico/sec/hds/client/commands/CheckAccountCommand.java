package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.CheckAccountRequest;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args){

    CheckAccountRequest checkAccountRequest = new CheckAccountRequest();

    try {
      System.out.println(client.server.checkAccount(checkAccountRequest));
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