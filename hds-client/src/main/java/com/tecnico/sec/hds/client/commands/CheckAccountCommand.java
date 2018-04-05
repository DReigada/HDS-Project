package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.ApiException;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import io.swagger.client.model.Signature;
import io.swagger.client.model.TransactionInformation;

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
    StringBuilder response = new StringBuilder("Public Key: " + client.key.getValue() + "\n" + "Balance: "
        + checkAmountResponse.getAmount() + "\n");
    Signature signature = checkAmountResponse.getSignature();
    try {

      for (TransactionInformation transactionInformation : checkAmountResponse.getList()){
        response.append(getTransactionListMessage(transactionInformation) + "\n");
      }

      if(client.cryptoAgent.verifyBankSignature(response.toString() , signature.getValue()))
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

  private String getTransactionListMessage(TransactionInformation transaction){
    String transactionListMessage = "";
    transactionListMessage += "Transaction ID: " + transaction.getTransID() + "\n";
    transactionListMessage += "Source Key: " + transaction.getSourceKey() + "\n";
    transactionListMessage += "Destination Key: " + transaction.getDestKey() + "\n";
    transactionListMessage += "Amount: " + transaction.getAmount() + "\n";
    transactionListMessage += "Pending: " + transaction.isPending() + "\n";
    transactionListMessage += "Received: " + transaction.isReceive() + "\n";
    transactionListMessage += "Signature: " + transaction.getSignature().getValue() + "\n";
    transactionListMessage += "Hash: " + transaction.getHash().getValue() + "\n";
    return transactionListMessage;
  }
}
