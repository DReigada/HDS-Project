package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
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
    CheckAccountRequest checkAccountRequest = new CheckAccountRequest().publicKey(client.key);

    CheckAccountResponse checkAmountResponse = client.server.checkAccount(checkAccountRequest);

    Signature signature = checkAmountResponse.getSignature();
    try {
      if(client.cryptoAgent.verifyBankSignature(checkAmountResponse.getMessage(), signature.getValue()))
        System.out.println(checkAmountResponse.getMessage());
      else
        System.out.print("Enexpected error from server. \n Try Again Later.");
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
