package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.client.commands.util.TransactionGetter;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.Signature;
import io.swagger.client.model.TransactionInformation;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class CheckAccountCommand extends AbstractCommand {
  private static final String name = "check_account";

  @Override
  public void doRun(Client client, String[] args) throws ApiException {

    CheckAccountRequest checkAccountRequest = new CheckAccountRequest().publicKey(client.key);

    CheckAccountResponse checkAccountResponse = client.server.checkAccount(checkAccountRequest);
    StringBuilder response = new StringBuilder();
    Signature signature = checkAccountResponse.getSignature();
    try {

      if (checkAccountResponse.getHistory() != null) {
        response.append(TransactionGetter.getTransactionListMessage(checkAccountResponse.getHistory()));
      }

      if (checkAccountResponse.getPending() != null) {
        response.append(TransactionGetter.getTransactionListMessage(checkAccountResponse.getPending()));
      }

      if (client.cryptoAgent.verifyBankSignature(response.toString(), signature.getValue())) {
        System.out.println("Public Key: " + client.key);
        long Balance = getBalance(checkAccountResponse.getHistory());
        System.out.println(response);
      } else {
        System.out.print("Unexpected error from server. \n Try Again Later.");
      }
    } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
      e.printStackTrace();
    }
  }

  public long getBalance(List<TransactionInformation> transactions){
    return transactions.stream().mapToLong(t -> t.isReceive() ? Long.valueOf(t.getAmount()) : -Long.valueOf(t.getAmount())).sum();
  }

  @Override
  public String getName() {
    return name;
  }
}