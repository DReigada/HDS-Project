package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) {
    Hash hash = new Hash();
    hash.setValue(args[0]);

    GetTransactionRequest getTransactionRequest = new GetTransactionRequest();

    getTransactionRequest.setHash(hash);

    GetTransactionResponse getTransactionResponse;
    try {
      getTransactionResponse = client.server.getTransaction(getTransactionRequest);

      if(!(getTransactionResponse.getTransaction() != null)){

        System.out.println("Transaction does not exist");

      }

      TransactionInformation transaction = getTransactionResponse.getTransaction();

      ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

      PubKey sourceKey = new PubKey();

      sourceKey.setValue(transaction.getSourceKey());

      receiveAmountRequest.setSourceKey(sourceKey);

      receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));

      receiveAmountRequest.setTransHash(hash);

      String receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest,
          transaction.getAmount(), hash.getValue());

      System.out.println(receiveAmountResponse);

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
