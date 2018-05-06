package com.tecnico.sec.hds.client.commands;

import com.tecnico.sec.hds.client.Client;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

public class ReceiveAmountCommand extends AbstractCommand {
  private static final String name = "receive_amount";

  @Override
  public void doRun(Client client, String[] args) {
    Hash hash = new Hash().value(args[0].trim());

    CheckAccountRequest checkAccountRequest = new CheckAccountRequest();

    Optional<Tuple<CheckAccountResponse, Long>> response = client.server.checkAccount(checkAccountRequest);

    if(response.isPresent()) {

      Optional<TransactionInformation> lastTransaction = response.map( r -> r.first.getHistory().get(0));

      Optional<TransactionInformation> receivedTransaction =
          response.get().first.getPending().stream()
              .filter(a -> a.getSendHash().getValue().equals(hash.getValue()))
              .findFirst();

      try {
        if (lastTransaction.isPresent() && receivedTransaction.isPresent()) {
          TransactionInformation transaction = receivedTransaction.get();
          ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest();

          receiveAmountRequest.setSourceKey(new PubKey().value(transaction.getSourceKey()));
          receiveAmountRequest.setDestKey(new PubKey().value(transaction.getDestKey()));
          receiveAmountRequest.amount(Integer.valueOf(transaction.getAmount()));
          receiveAmountRequest.setTransHash(transaction.getSendHash());
          receiveAmountRequest.setTransHash(receivedTransaction.get().getSendHash());

          boolean receiveAmountResponse = client.server.receiveAmount(receiveAmountRequest, lastTransaction.get());

          if (receiveAmountResponse) {
            System.out.println("Receive amount successful");
          } else {
            System.out.println("Failed to call receive amount");
          }
        }

      } catch (CertificateException | InvalidKeySpecException | InvalidKeyException | SignatureException
          | KeyStoreException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
        System.out.println("Failed to call receive amount");
        e.printStackTrace();
      }

    } else {
      System.out.println("Failed to check your account");
    }
  }

  @Override
  public String getName() {
    return name;
  }
}
