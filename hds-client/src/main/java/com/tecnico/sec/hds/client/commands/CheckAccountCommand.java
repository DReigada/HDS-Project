package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAmountRequest;
import io.swagger.client.model.CheckAmountResponse;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {
    CheckAmountRequest checkAmountRequest = new CheckAmountRequest().publicKey(client.key);
    CheckAmountResponse checkAmountResponse = client.server.checkAmount(checkAmountRequest);

    Signature signature = checkAmountResponse.getSignature();
    try {
      client.cryptoAgent.verifyBankSignature(checkAmountResponse.getMessage(), signature.getValue());
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }

    System.out.println(checkAmountResponse);
  }

  @Override
  public String getName() {
    return name;
  }
}
