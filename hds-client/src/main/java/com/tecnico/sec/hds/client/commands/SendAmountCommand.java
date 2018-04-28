package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.*;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class SendAmountCommand extends AbstractCommand {
  private static final String name = "send_amount";

  @Override
  public void doRun(Client client, String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException, InvalidKeySpecException, CertificateException, KeyStoreException, UnrecoverableKeyException {
    PubKey destKey = new PubKey();

    destKey.setValue(args[0]);

    Hash lastHash = client.getLastHash();

    Integer amount = Integer.parseInt(args[1]);

    Signature signature = new Signature();

    signature.setValue(client.cryptoAgent.generateSignature(client.key.getValue() + destKey.getValue() + amount.toString() + lastHash.getValue()));
    SendAmountRequest sendAmount = new SendAmountRequest();
    sendAmount.setSourceKey(client.key);
    sendAmount.setDestKey(destKey);
    sendAmount.setAmount(amount);
    sendAmount.setLastHash(lastHash);
    sendAmount.setSignature(signature);

    SendAmountResponse sendAmountResponse = client.server.sendAmount(sendAmount);

    lastHash = sendAmountResponse.getNewHash();

    Signature bankSignature = sendAmountResponse.getSignature();


    if (client.cryptoAgent.verifyBankSignature(lastHash.getValue() + sendAmountResponse.getMessage(), bankSignature.getValue())) {
      if (lastHash.getValue() != null) {
        client.setLastHash(lastHash);
      }
      System.out.println(sendAmountResponse.getMessage());
    } else {
      System.out.println("I caught you fake!!");
    }

  }


  @Override
  public String getName() {
    return name;
  }
}
