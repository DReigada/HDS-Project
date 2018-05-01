package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.RegisterRequest;
import io.swagger.client.model.RegisterResponse;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class RegisterCommand extends AbstractCommand {
  private static final String name = "register";

  @Override
  public void doRun(Client client, String[] args) throws ApiException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, CertificateException, KeyStoreException, UnrecoverableKeyException {
    RegisterRequest request = new RegisterRequest().publicKey(client.key);
    Signature sign = new Signature().value(client.cryptoAgent.generateSignature(client.key.getValue()));
    request.setSignature(sign);
    RegisterResponse response = client.server.register(request);
    sign = response.getSignature();

    try {
      if (client.cryptoAgent.verifyBankSignature(response.getMessage() + response.getHash().getValue(), sign.getValue())) {
        System.out.println(response.getMessage());
        client.setLastHash(response.getHash());
      } else {
        System.out.print("Unexpected error from server. \n Try Again Later.");
      }
    } catch (IOException | InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
