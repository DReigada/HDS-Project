package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class RegisterCommand extends AbstractCommand {
  private static final String name = "register";

  @Override
  public void doRun(Client client, String[] args){
    try {
      System.out.print(client.server.register());
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
