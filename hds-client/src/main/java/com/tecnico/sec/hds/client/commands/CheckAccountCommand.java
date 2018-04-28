package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {

    CheckAccountRequest checkAccountRequest = new CheckAccountRequest().publicKey(client.key);

    CheckAccountResponse checkAmountResponse = client.server.checkAccount(checkAccountRequest);
    StringBuilder response = new StringBuilder("Public Key: " + client.key.getValue() + "\n" + "Balance: "
        + checkAmountResponse.getAmount() + "\n");
    Signature signature = checkAmountResponse.getSignature();
    try {

      if (checkAmountResponse.getList() != null) {
          response.append(TransactionGetter.getTransactionListMessage(checkAmountResponse.getList()));
      }

      if (client.cryptoAgent.verifyBankSignature(response.toString(), signature.getValue()))
        System.out.println(response);
      else
        System.out.print("Unexpected error from server. \n Try Again Later.");
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return name;
  }
}