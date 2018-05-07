package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.SendAmountRequest;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class SendAmountCommand extends AbstractCommand {
  private static final String name = "send_amount";

  @Override
  public void doRun(Client client, String[] args) {
    PubKey destKey = new PubKey();
    destKey.setValue(args[0].trim());
    Integer amount = Integer.parseInt(args[1].trim());

    SendAmountRequest sendAmount = new SendAmountRequest();
    sendAmount.setDestKey(destKey);
    sendAmount.setAmount(amount);

    try {
      System.out.println(client.server.sendAmount(sendAmount));
    } catch (GeneralSecurityException | IOException e) {
      e.printStackTrace();
    }

  }


  @Override
  public String getName() {
    return name;
  }
}
